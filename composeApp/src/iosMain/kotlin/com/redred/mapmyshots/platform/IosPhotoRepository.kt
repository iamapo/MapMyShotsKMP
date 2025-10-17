package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IosPhotoRepository : PhotoRepository {
    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun listImagesBetween(
        min: Instant,
        max: Instant
    ): List<Asset> {
        TODO("Not yet implemented")
    }

}
