package com.redred.mapmyshots.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.redred.mapmyshots.model.Asset
import com.seiko.imageloader.rememberImagePainter

@Composable
actual fun AssetThumbnail(
    asset: Asset,
    modifier: Modifier
) {
    val painter = rememberImagePainter(asset.uri)

    Image(
        painter = painter,
        contentDescription = asset.displayName,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}