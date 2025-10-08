package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.PhotoRepository
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SimilarityService(
    private val repo: PhotoRepository,
    private val exif: ExifService
) {
    @OptIn(ExperimentalTime::class)
    suspend fun findByTimeAndGps(target: Asset, threshold: kotlin.time.Duration): List<Asset> {
        val min = Instant.fromEpochMilliseconds(
            target.takenAt.toEpochMilliseconds() - threshold.inWholeMilliseconds
        )
        val max = Instant.fromEpochMilliseconds(
            target.takenAt.toEpochMilliseconds() + threshold.inWholeMilliseconds
        )
        val candidates = repo.listImagesBetween(min, max)
        return candidates.filter { it.id != target.id }
            .filter { exif.getLatLon(it) != null }
    }
}