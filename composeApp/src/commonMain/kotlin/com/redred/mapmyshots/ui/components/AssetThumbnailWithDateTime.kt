package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.AssetThumbnail
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun AssetThumbnailWithDateTime(
    asset: Asset,
    modifier: Modifier = Modifier
) {
    val shape = MapMyShotsShapes.hero

    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
    ) {
        AssetThumbnail(
            asset = asset,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MapMyShotsColors.imageOverlay)
        )
    }
}

@Preview
@Composable
private fun AssetThumbnailWithDateTimePreview() {
    MaterialTheme {
        AssetThumbnailWithDateTime(
            asset = previewAsset(),
            modifier = Modifier.height(MapMyShotsSizes.thumbnailPreviewHeight)
        )
    }
}
