package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class AssetPage(
    val items: List<Asset>,
    val endReached: Boolean
)

interface PhotoRepository {
    suspend fun listImagesPage(offset: Int, limit: Int): AssetPage
    suspend fun listAllImages(limitPerAlbum: Int = 0): List<Asset>
    @OptIn(ExperimentalTime::class)
    suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset>
    suspend fun deleteAsset(asset: Asset): Boolean
}

interface ExifPlatform {
    suspend fun readLatLon(asset: Asset): Pair<Double, Double>?
    suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean
}

interface GeocoderPlatform {
    suspend fun reverseGeocode(lat: Double, lon: Double): String?
}