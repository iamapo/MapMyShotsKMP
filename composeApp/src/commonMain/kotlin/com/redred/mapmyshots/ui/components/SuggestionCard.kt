package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.theme.*

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
            .shadow(
                elevation = 4.dp,
                shape = MapMyShotsShapes.card
            ),
        shape = MapMyShotsShapes.card,
        colors = CardDefaults.elevatedCardColors(containerColor = MapMyShotsColors.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MapMyShotsSizes.suggestionHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SuggestionThumbnail(
                suggestion = suggestion,
                modifier = Modifier.fillMaxHeight()
            )

            Spacer(Modifier.width(MapMyShotsSpacing.lg))

            SuggestionInfo(
                photo = photo,
                suggestion = suggestion,
                place = place,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = MapMyShotsSpacing.lg)
            )

            if (isApplied) {
                SuggestionApplyAction(applied = true)
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
