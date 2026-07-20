package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.AssetThumbnail
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.missing_location_badge
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotoGridCard(
    photo: Asset,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val cardShape = MapMyShotsShapes.card

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AssetThumbnail(
                asset = photo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MapMyShotsSizes.photoCardImageHeight)
            )

            StatusBadge(
                text = stringResource(Res.string.missing_location_badge),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(MapMyShotsSpacing.md)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(MapMyShotsSpacing.lg)
            ) {
                Text(
                    text = formatTime(photo.takenAt),
                    fontSize = MapMyShotsTypography.suggestionTitle,
                    fontWeight = FontWeight.Bold,
                    color = MapMyShotsColors.onImage
                )
                Text(
                    text = formatDate(photo.takenAt),
                    fontSize = MapMyShotsTypography.imageLabel,
                    color = MapMyShotsColors.onImage
                )
            }
        }
    }
}

@Preview
@Composable
private fun PhotoGridCardPreview() {
    MaterialTheme {
        PhotoGridCard(
            photo = previewAsset(),
            onClick = {},
            onLongClick = {}
        )
    }
}
