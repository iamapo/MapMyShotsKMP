package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.applied
import mapmyshots.composeapp.generated.resources.apply
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SuggestionCard(
    photo: Asset,
    suggestion: Asset,
    place: String,
    isApplied: Boolean = false,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isApplied) {
                    Modifier.border(
                        width = MapMyShotsStroke.selected,
                        color = MapMyShotsColors.primary,
                        shape = MapMyShotsShapes.card
                    )
                } else {
                    Modifier
                }
            )
            .clickable(enabled = !isApplied, onClick = onClick)
            .shadow(elevation = 1.dp, shape = MapMyShotsShapes.card),
        shape = MapMyShotsShapes.card,
        colors = CardDefaults.elevatedCardColors(containerColor = MapMyShotsColors.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MapMyShotsSpacing.xxl),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MapMyShotsSpacing.xl)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MapMyShotsColors.tealBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = MapMyShotsColors.teal)
                }
                Spacer(Modifier.width(MapMyShotsSpacing.lg))
                SuggestionInfo(
                    photo = photo,
                    suggestion = suggestion,
                    place = place,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionThumbnail(suggestion = suggestion)
                Spacer(Modifier.width(MapMyShotsSpacing.lg))
                Text(
                    text = suggestion.displayName.orEmpty(),
                    fontSize = MapMyShotsTypography.badge,
                    color = MapMyShotsColors.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MapMyShotsShapes.pill)
                    .background(if (isApplied) MapMyShotsColors.teal else MapMyShotsColors.primary)
                    .padding(vertical = MapMyShotsSpacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isApplied) {
                        Icon(Icons.Filled.Check, null, tint = MapMyShotsColors.onImage)
                        Spacer(Modifier.width(MapMyShotsSpacing.sm))
                    }
                    Text(
                        text = stringResource(if (isApplied) Res.string.applied else Res.string.apply),
                        fontSize = MapMyShotsTypography.metadata,
                        fontWeight = FontWeight.Bold,
                        color = MapMyShotsColors.onImage
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SuggestionCardPreview() {
    MaterialTheme {
        SuggestionCard(
            photo = previewAsset(),
            suggestion = previewSuggestionAsset(),
            place = "Munich · Marienplatz",
            isApplied = true,
            onClick = {}
        )
    }
}
