package com.redred.mapmyshots.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.toImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGSizeMake
import platform.Photos.PHAsset
import platform.Photos.PHCachingImageManager
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeOpportunistic
import platform.Photos.PHImageRequestOptionsResizeModeFast

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AssetThumbnail(
    asset: Asset,
    modifier: Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val targetWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(120) }
        val targetHeightPx = with(density) { maxHeight.roundToPx().coerceAtLeast(120) }

        var bitmap by remember(asset.id, targetWidthPx, targetHeightPx) {
            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
        }

        DisposableEffect(asset.id, targetWidthPx, targetHeightPx) {
            bitmap = null

            val fetchResult = PHAsset.fetchAssetsWithLocalIdentifiers(
                listOf(asset.id),
                null
            )
            val phAsset = fetchResult.firstObject() as? PHAsset
            if (phAsset == null) {
                onDispose { }
            } else {
                val options = PHImageRequestOptions().apply {
                    resizeMode = PHImageRequestOptionsResizeModeFast
                    deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic
                    networkAccessAllowed = true
                    synchronous = false
                }

                val requestId = imageManager.requestImageForAsset(
                    asset = phAsset,
                    targetSize = CGSizeMake(targetWidthPx.toDouble(), targetHeightPx.toDouble()),
                    contentMode = PHImageContentModeAspectFill,
                    options = options
                ) { image, _ ->
                    image?.toImageBitmap()?.let { bitmap = it }
                }

                onDispose {
                    imageManager.cancelImageRequest(requestId)
                }
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = asset.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
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
}

@OptIn(ExperimentalForeignApi::class)
private val imageManager: PHCachingImageManager = PHCachingImageManager()
