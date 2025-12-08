package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Photos.PHAsset

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

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean {
        // iOS erlaubt das direkte Bearbeiten von EXIF/Location in der Photos Library
        // eigentlich nicht so einfach wie auf Android (ExifInterface).
        // Man müsste über PHPhotoLibrary.performChanges neue Assets mit geänderten
        // Metadaten anlegen. Das ist ziemlich aufwendig und oft unerwünscht.
        //
        // Für den Anfang: nicht unterstützt -> false zurückgeben.
        return false
    }
}