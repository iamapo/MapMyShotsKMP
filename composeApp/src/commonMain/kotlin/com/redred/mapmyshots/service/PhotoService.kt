package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.ExperimentalTime

data class PhotoPageResult(
    val items: List<Asset>,
    val candidateCount: Int
)

class PhotoService(
    private val repo: PhotoRepository,
    private val exif: ExifService
) {
    /**
     * Lädt maxCount neueste Photos und filtert jene OHNE Location.
     * Progress zeigt:
     * - scanned: wie viele Kandidaten geprüft wurden
     * - total: wie viele Kandidaten insgesamt geprüft werden (<= maxCount)
     * - found: wie viele ohne Location bisher gefunden wurden
     */
    @OptIn(ExperimentalTime::class)
    suspend fun loadPhotosPage(
        maxCount: Int,
        batchSize: Int = 24,
        onProgress: (scanned: Int, total: Int, found: Int) -> Unit = { _, _, _ -> }
    ): PhotoPageResult = coroutineScope {

        val candidates = repo.listAllImages(limitPerAlbum = maxCount).take(maxCount)
        val total = candidates.size

        var scanned = 0
        var found = 0
        onProgress(scanned, total, found)

        val withoutLocation = candidates
            .chunked(batchSize)
            .flatMap { batch ->
                val batchResults: List<Asset?> = batch.map { asset ->
                    async(Dispatchers.IO) {
                        val hasLatLon = exif.getLatLon(asset) != null
                        if (!hasLatLon) asset else null
                    }
                }.awaitAll()

                val hits = batchResults.filterNotNull()
                found += hits.size
                scanned += batch.size
                onProgress(scanned, total, found)

                hits
            }
            .sortedByDescending { it.takenAt }

        PhotoPageResult(
            items = withoutLocation,
            candidateCount = total
        )
    }
}
