package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAsset
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHFetchOptions
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IOSPhotoRepository : PhotoRepository {

    @OptIn(ExperimentalTime::class, ExperimentalForeignApi::class)
    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> {
        val options = PHFetchOptions().apply {
            predicate = NSPredicate.predicateWithFormat(
                "mediaType == %d",
                PHAssetMediaTypeImage
            )

            sortDescriptors = listOf(
                NSSortDescriptor(key = "creationDate", ascending = false)
            )
        }

        val result = PHAsset.fetchAssetsWithOptions(options)
        val out = mutableListOf<Asset>()

        result.enumerateObjectsUsingBlock { obj, _, _ ->
            val phAsset = obj as PHAsset
            val id = phAsset.localIdentifier
            val date = phAsset.creationDate ?: NSDate()
            val tsMillis = (date.timeIntervalSince1970 * 1000.0).toLong()

            out += Asset(
                id = id,
                displayName = id,
                takenAt = Instant.fromEpochMilliseconds(tsMillis),
                uri = id
            )
        }
        return out
    }

    @OptIn(ExperimentalTime::class, ExperimentalForeignApi::class)
    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        val minDate = NSDate.dateWithTimeIntervalSince1970(
            min.toEpochMilliseconds().toDouble() / 1000.0
        )
        val maxDate = NSDate.dateWithTimeIntervalSince1970(
            max.toEpochMilliseconds().toDouble() / 1000.0
        )

        val options = PHFetchOptions().apply {
            predicate = NSPredicate.predicateWithFormat(
                "mediaType == %d AND creationDate >= %@ AND creationDate <= %@",
                PHAssetMediaTypeImage,
                minDate,
                maxDate
            )

            sortDescriptors = listOf(
                NSSortDescriptor(key = "creationDate", ascending = false)
            )
        }

        val result = PHAsset.fetchAssetsWithOptions(options)
        val out = mutableListOf<Asset>()

        result.enumerateObjectsUsingBlock { obj, _, _ ->
            val phAsset = obj as PHAsset
            val id = phAsset.localIdentifier
            val date = phAsset.creationDate ?: NSDate()
            val tsMillis = (date.timeIntervalSince1970 * 1000.0).toLong()

            out += Asset(
                id = id,
                displayName = id,
                takenAt = Instant.fromEpochMilliseconds(tsMillis),
                uri = id
            )
        }
        return out
    }
}