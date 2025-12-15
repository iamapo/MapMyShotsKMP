package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.ExperimentalTime


data class PhotoPageDelta(
    val newHits: List<Asset>,
    val scannedInChunk: Int,
    val foundInChunk: Int,
    val endReached: Boolean
)

class PhotoService(
    private val repo: PhotoRepository,
    private val exif: ExifService
) {

    @OptIn(ExperimentalTime::class)
    suspend fun loadNextChunkWithoutLocation(
        offset: Int,
        candidateLimit: Int,
        batchSize: Int = 24,
        onProgress: (scannedInChunk: Int, chunkTotal: Int, foundInChunk: Int) -> Unit = { _, _, _ -> }
    ): PhotoPageDelta = coroutineScope {

        val page = repo.listImagesPage(offset = offset, limit = candidateLimit)
        val candidates = page.items
        val chunkTotal = candidates.size

        var scanned = 0
        var found = 0
        onProgress(scanned, chunkTotal, found)

        val hits = candidates
            .chunked(batchSize)
            .flatMap { batch ->
                val res = batch.map { asset ->
                    async(Dispatchers.IO) {
                        val hasLatLon = exif.getLatLon(asset) != null
                        if (!hasLatLon) asset else null
                    }
                }.awaitAll()

                val batchHits = res.filterNotNull()
                found += batchHits.size
                scanned += batch.size
                onProgress(scanned, chunkTotal, found)

                batchHits
            }

        PhotoPageDelta(
            newHits = hits,                 // KEIN sort nötig: Kandidaten kommen schon newest-first, wir behalten Order
            scannedInChunk = chunkTotal,
            foundInChunk = found,
            endReached = page.endReached
        )
    }
}
