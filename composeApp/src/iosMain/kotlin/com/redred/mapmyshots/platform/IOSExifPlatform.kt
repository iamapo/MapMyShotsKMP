package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

class IOSExifPlatform : ExifPlatform {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
            listOf(asset.id),
            null
        )

        val phAsset = fetchResult.firstObject() as? PHAsset ?: return null
        val loc = phAsset.location ?: return null

        val coord = loc.coordinate()
        return coord.useContents {
            latitude to longitude
        }
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean =
        suspendCancellableCoroutine { cont ->
            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(asset.id), null)
            val phAsset = fetchResult.firstObject() as? PHAsset
            if (phAsset == null) {
                cont.resume(false)
                return@suspendCancellableCoroutine
            }

            val newLocation = CLLocation(latitude = lat, longitude = lon)

            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                {
                    val req = PHAssetChangeRequest.changeRequestForAsset(phAsset)
                    req.location = newLocation
                },
                completionHandler = { success, _ ->
                    if (cont.isActive) cont.resume(success)
                }
            )
        }
}