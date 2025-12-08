package com.redred.mapmyshots.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.toImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGSizeMake
import platform.Photos.PHAsset
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeOpportunistic
import platform.Photos.PHImageRequestOptionsResizeModeFast
import kotlin.coroutines.resume

@Composable
actual fun AssetThumbnail(
    asset: Asset,
    modifier: Modifier
) {
    var bitmap by remember(asset.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(asset.id) {
        bitmap = loadThumbnail(asset)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = asset.displayName,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Platzhalter, bis das Bild geladen ist
        Box(
            modifier = modifier.background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.uri.take(12) + "…",
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun loadThumbnail(asset: Asset)
        : androidx.compose.ui.graphics.ImageBitmap? =
    suspendCancellableCoroutine { cont ->

        val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
            listOf(asset.id),
            null
        )
        val phAsset = fetchResult.firstObject() as? PHAsset

        if (phAsset == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val options = PHImageRequestOptions().apply {
            resizeMode = PHImageRequestOptionsResizeModeFast
            deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic
            networkAccessAllowed = true
            synchronous = false
        }

        val targetSize = CGSizeMake(300.0, 300.0)
        val manager = PHImageManager.defaultManager()

        manager.requestImageForAsset(
            asset = phAsset,
            targetSize = targetSize,
            contentMode = PHImageContentModeAspectFill,
            options = options
        ) { image, _ ->
            if (!cont.isActive) return@requestImageForAsset

            val bmp = image?.toImageBitmap()
            cont.resume(bmp)
        }

}