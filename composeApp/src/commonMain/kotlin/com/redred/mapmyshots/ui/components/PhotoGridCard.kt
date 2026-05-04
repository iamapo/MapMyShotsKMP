package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = MapMyShotsStroke.selected,
                        color = MapMyShotsColors.primary,
                        shape = MapMyShotsShapes.card
                    )
                } else {
                    Modifier
                }
            ),
        shape = MapMyShotsShapes.card,
        colors = CardDefaults.elevatedCardColors(containerColor = MapMyShotsColors.surface)
    ) {
        Box {
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

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(MapMyShotsSpacing.md)
                        .size(MapMyShotsSizes.selectedBadge)
                        .clip(CircleShape)
                        .background(MapMyShotsColors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MapMyShotsColors.onImage
                    )
                }
            }
        }

        Text(
            text = "${formatDate(photo.takenAt)} · ${formatTime(photo.takenAt)}",
            modifier = Modifier.padding(horizontal = MapMyShotsSpacing.xl, vertical = MapMyShotsSpacing.lg),
            fontSize = MapMyShotsTypography.cardDate,
            color = MapMyShotsColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun PhotoGridCardPreview() {
    MaterialTheme {
        PhotoGridCard(
            photo = previewAsset(),
            selected = true,
            onClick = {},
            onLongClick = {}
        )
    }
}
