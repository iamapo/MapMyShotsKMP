package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToLong

class ExifService(private val exifPlatform: ExifPlatform) {
    // Accessed from parallel IO coroutines, so every map access goes through the mutex.
    private val cacheMutex = Mutex()
    private val latLonMap = mutableMapOf<String, Pair<Double, Double>?>()
    private val placeNameMap = mutableMapOf<Pair<Long, Long>, String>()

    suspend fun hasLatLon(asset: Asset): Boolean {
        asset.hasLocation?.let { return it }
        return getLatLon(asset) != null
    }

    /** Returns null when the photo has no GPS data or cannot be read. Read errors are not cached. */
    suspend fun getLatLon(asset: Asset): Pair<Double, Double>? {
        cacheMutex.withLock {
            if (latLonMap.containsKey(asset.id)) {
                return latLonMap[asset.id]
            }
        }
        val latLon = try {
            exifPlatform.readLatLon(asset)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            return null
        }
        cacheMutex.withLock {
            latLonMap[asset.id] = latLon
        }
        return latLon
    }

    suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean =
        exifPlatform.writeLatLon(asset, lat, lon).also { success ->
            if (success) {
                cacheMutex.withLock {
                    latLonMap[asset.id] = lat to lon
                }
            }
        }

    suspend fun getLocationName(asset: Asset, geocoder: GeocoderPlatform): String? {
        val latLon = getLatLon(asset) ?: return null
        return getLocationName(latLon.first, latLon.second, geocoder)
    }

    /**
     * Geocodes once per ~100 m cell, because platform geocoders (especially CLGeocoder) are rate limited.
     * Failed lookups are not cached so they can be retried later.
     */
    suspend fun getLocationName(lat: Double, lon: Double, geocoder: GeocoderPlatform): String? {
        val key = (lat * 1_000).roundToLong() to (lon * 1_000).roundToLong()
        cacheMutex.withLock {
            placeNameMap[key]?.let { return it }
        }
        val name = geocoder.reverseGeocode(lat, lon) ?: return null
        cacheMutex.withLock {
            placeNameMap[key] = name
        }
        return name
    }
}
