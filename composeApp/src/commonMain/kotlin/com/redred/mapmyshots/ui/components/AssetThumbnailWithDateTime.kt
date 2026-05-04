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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.AssetThumbnail
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun AssetThumbnailWithDateTime(
    asset: Asset,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AssetThumbnail(
            asset = asset,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MapMyShotsColors.imageOverlay)
        )
        Text(
            text = asset.displayName ?: formatTakenAt(asset.takenAt),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(MapMyShotsSpacing.xl),
            color = MapMyShotsColors.onImage,
            fontSize = MapMyShotsTypography.imageLabel,
            fontWeight = FontWeight.SemiBold
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
