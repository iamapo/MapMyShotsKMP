package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.ExperimentalTime

class PhotoService(
    private val repo: PhotoRepository,
    private val exif: ExifService) {

    @OptIn(ExperimentalTime::class)
    suspend fun loadPhotosPage(
        maxCount: Int
    ): List<Asset> = coroutineScope {
        val all = repo.listAllImages()
        all.chunked(maxCount).flatMap { batch ->
            batch.map { asset ->
                async(Dispatchers.IO) {
                    val hasLatLon = exif.getLatLon(asset) != null
                    if (!hasLatLon) asset else null
                }
            }.awaitAll().filterNotNull()
        }.sortedByDescending { it.takenAt }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun loadPhotosWithoutLocation(
        maxPerAlbum: Int = 20,
        batchSize: Int = 24
    ): List<Asset> = coroutineScope {
        val all = repo.listAllImages(maxPerAlbum)
        all.chunked(batchSize).flatMap { batch ->
            batch.map { asset ->
                async(Dispatchers.IO) {
                    val hasLatLon = exif.getLatLon(asset) != null
                    if (!hasLatLon) asset else null
                }
            }.awaitAll().filterNotNull()
        }.sortedByDescending { it.takenAt }
    }
}