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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = cardShape)
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = MapMyShotsColors.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
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
                    .padding(MapMyShotsSpacing.xs)
            )
        }

        Text(
            text = "${formatDate(photo.takenAt)} · ${formatTime(photo.takenAt)}",
            modifier = Modifier.padding(horizontal = MapMyShotsSpacing.sm, vertical = MapMyShotsSpacing.sm),
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
            onClick = {},
            onLongClick = {}
        )
    }
}
