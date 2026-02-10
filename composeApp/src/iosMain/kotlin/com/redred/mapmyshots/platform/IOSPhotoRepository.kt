package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSPredicate
import platform.Foundation.NSSortDescriptor
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHFetchOptions
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IOSPhotoRepository : PhotoRepository {

    @OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> {
        val options = PHFetchOptions().apply {
            sortDescriptors = listOf(NSSortDescriptor(key = "creationDate", ascending = false))

            if (limitPerAlbum > 0) {
                try {
                    fetchLimit = limitPerAlbum.toULong()
                } catch (_: Throwable) {
                }
            }
        }

        val fetchResult = PHAsset.fetchAssetsWithMediaType(
            mediaType = PHAssetMediaTypeImage,
            options = options
        )

        val out = mutableListOf<Asset>()

        fetchResult.enumerateObjectsUsingBlock { obj, _, stop ->
            val asset = obj as? PHAsset ?: return@enumerateObjectsUsingBlock

            val id = asset.localIdentifier
            val date = asset.creationDate ?: NSDate()
            val tsMillis = (date.timeIntervalSince1970 * 1000.0).toLong()

            out += Asset(
                id = id,
                displayName = "",
                takenAt = Instant.fromEpochMilliseconds(tsMillis),
                uri = "ph://$id"
            )

            if (limitPerAlbum > 0 && out.size >= limitPerAlbum) {
                stop?.pointed?.value = true
            }
        }

        return out
    }

    @OptIn(ExperimentalTime::class, ExperimentalForeignApi::class)
    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        val minDate = NSDate.dateWithTimeIntervalSince1970(min.toEpochMilliseconds().toDouble() / 1000.0)
        val maxDate = NSDate.dateWithTimeIntervalSince1970(max.toEpochMilliseconds().toDouble() / 1000.0)

        val options = PHFetchOptions().apply {
            predicate = NSPredicate.predicateWithFormat(
                "mediaType == %d AND creationDate >= %@ AND creationDate <= %@",
                PHAssetMediaTypeImage,
                minDate,
                maxDate
            )
            sortDescriptors = listOf(NSSortDescriptor(key = "creationDate", ascending = false))
        }

        val result = PHAsset.fetchAssetsWithOptions(options)
        val out = mutableListOf<Asset>()

        result.enumerateObjectsUsingBlock { obj, _, _ ->
            val phAsset = obj as? PHAsset ?: return@enumerateObjectsUsingBlock

            val id = phAsset.localIdentifier
            val date = phAsset.creationDate ?: NSDate()
            val tsMillis = (date.timeIntervalSince1970 * 1000.0).toLong()

            out += Asset(
                id = id,
                displayName = id,
                takenAt = Instant.fromEpochMilliseconds(tsMillis),
                uri = "ph://$id"
            )
        }

        return out
    }

    @OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        val options = PHFetchOptions().apply {
            sortDescriptors = listOf(NSSortDescriptor(key = "creationDate", ascending = false))
            if (limit > 0) {
                try {
                    fetchLimit = (offset + limit).toULong()
                } catch (_: Throwable) { /* ignore */ }
            }
        }

        val fetchResult = PHAsset.fetchAssetsWithMediaType(
            mediaType = PHAssetMediaTypeImage,
            options = options
        )

        val out = mutableListOf<Asset>()
        var index = 0

        fetchResult.enumerateObjectsUsingBlock { obj, _, stop ->
            val asset = obj as? PHAsset ?: return@enumerateObjectsUsingBlock

            if (index >= offset && out.size < limit) {
                val id = asset.localIdentifier
                val date = asset.creationDate ?: NSDate()
                val tsMillis = (date.timeIntervalSince1970 * 1000.0).toLong()

                out += Asset(
                    id = id,
                    displayName = "",
                    takenAt = Instant.fromEpochMilliseconds(tsMillis),
                    uri = "ph://$id"
                )
            }

            index += 1

            if (out.size >= limit) {
                stop?.pointed?.value = true
            }
        }

        val endReached = out.size < limit
        return AssetPage(items = out, endReached = endReached)
    }

    override suspend fun deleteAsset(asset: Asset): Boolean =
        suspendCancellableCoroutine { cont ->
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(asset.id), null)
            val phAsset = fetchResult.firstObject() as? PHAsset
            if (phAsset == null) {
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                {
                    PHAssetChangeRequest.deleteAssets(fetchResult)
                },
                completionHandler = { success, _ ->
                    if (cont.isActive) cont.resume(success)
                }
            )
        }

}