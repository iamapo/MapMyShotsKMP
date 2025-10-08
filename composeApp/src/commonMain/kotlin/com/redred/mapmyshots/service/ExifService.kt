package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform

class ExifService(private val exifPlatform: ExifPlatform) {
    private val cache = mutableMapOf<String, Pair<Double, Double>?>()
    private val nameCache = mutableMapOf<String, String>()

    suspend fun getLat(asset: Asset): Double? = getLatLon(asset)?.first
    suspend fun getLon(asset: Asset): Double? = getLatLon(asset)?.second

    suspend fun getLatLon(asset: Asset): Pair<Double, Double>? {
        return cache[asset.id] ?: exifPlatform.readLatLon(asset).also { cache[asset.id] = it }
    }

    suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean =
        exifPlatform.writeLatLon(asset, lat, lon)

    suspend fun getLocationName(asset: Asset, geocoder: GeocoderPlatform): String {
        nameCache[asset.id]?.let { return it }
        val pair = getLatLon(asset) ?: return "No Location Data"
        val name = geocoder.reverseGeocode(pair.first, pair.second) ?: "Location Unknown"
        nameCache[asset.id] = name
        return name
    }
}