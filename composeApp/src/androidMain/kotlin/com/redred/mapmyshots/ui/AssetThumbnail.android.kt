package com.redred.mapmyshots.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import com.redred.mapmyshots.model.Asset
import com.seiko.imageloader.rememberImagePainter
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun AssetThumbnail(
    asset: Asset,
    modifier: Modifier
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = asset.displayName,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    val painter = rememberImagePainter(asset.uri)

    Image(
        painter = painter,
        contentDescription = asset.displayName,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
