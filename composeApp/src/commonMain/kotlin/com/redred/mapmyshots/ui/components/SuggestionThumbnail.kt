package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.AssetThumbnail
import com.redred.mapmyshots.ui.theme.MapMyShotsShapes
import com.redred.mapmyshots.ui.theme.MapMyShotsSizes

@Composable
internal fun SuggestionThumbnail(
    suggestion: Asset,
    modifier: Modifier = Modifier
) {
    AssetThumbnail(
        asset = suggestion,
        modifier = modifier
            .width(MapMyShotsSizes.suggestionImageWidth)
            .fillMaxHeight()
            .clip(MapMyShotsShapes.suggestionImage)
    )
}

@Preview
@Composable
private fun SuggestionThumbnailPreview() {
    MaterialTheme {
        SuggestionThumbnail(suggestion = previewSuggestionAsset())
    }
}
