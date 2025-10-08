package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface PhotoRepository {
    suspend fun listAllImages(limitPerAlbum: Int = 20): List<Asset>
    @OptIn(ExperimentalTime::class)
    suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset>
}

interface ExifPlatform {
    suspend fun readLatLon(asset: Asset): Pair<Double, Double>?
    suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean
}

interface GeocoderPlatform {
    suspend fun reverseGeocode(lat: Double, lon: Double): String?
}