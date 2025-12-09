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
    private val exif: ExifService
) {

    // Cache: welche Assets haben KEINE Location / welche schon
    private val noLocation = mutableSetOf<String>()
    private val withLocation = mutableSetOf<String>()

    // Einmal geladene, sortierte Liste aller Assets
    private var allSorted: List<Asset>? = null

    @OptIn(ExperimentalTime::class)
    private suspend fun ensureAllSorted(): List<Asset> {
        val cached = allSorted
        if (cached != null) return cached

        val all = repo.listAllImages()
            .sortedByDescending { it.takenAt }
        allSorted = all
        return all
    }

    /**
     * Liefert eine "Page" von Assets OHNE Location.
     * pagingOffset = wie viele Assets im Gesamtdatensatz bereits "verbraucht" wurden
     * pageSize = wie viele neue Assets aus dem Gesamtdatensatz maximal inspizieren
     */
    @OptIn(ExperimentalTime::class)
    suspend fun loadPhotosPage(
        pagingOffset: Int,
        pageSize: Int
    ): List<Asset> = coroutineScope {
        val all = ensureAllSorted()

        if (pagingOffset >= all.size) return@coroutineScope emptyList()

        val slice = all
            .drop(pagingOffset)
            .take(pageSize)

        slice.map { asset ->
            async(Dispatchers.IO) {
                val id = asset.id

                // Cache-Hit?
                when {
                    withLocation.contains(id) -> null
                    noLocation.contains(id) -> asset
                    else -> {
                        val hasLatLon = exif.getLatLon(asset) != null
                        if (hasLatLon) {
                            withLocation += id
                            null
                        } else {
                            noLocation += id
                            asset
                        }
                    }
                }
            }
        }.awaitAll()
            .filterNotNull()
        // slice ist schon sortiert, sortieren wäre eigentlich nicht mehr nötig
    }
}