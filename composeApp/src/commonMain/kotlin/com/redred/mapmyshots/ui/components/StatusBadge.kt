package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    success: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(MapMyShotsShapes.pill)
            .background(if (success) MapMyShotsColors.successBackground else MapMyShotsColors.warningBackground)
            .padding(horizontal = MapMyShotsSpacing.md, vertical = MapMyShotsSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = if (success) MapMyShotsColors.success else MapMyShotsColors.warning
        )

        Spacer(Modifier.width(MapMyShotsSpacing.xs))

        Text(
            text = text,
            fontSize = MapMyShotsTypography.caption,
            fontWeight = FontWeight.SemiBold,
            color = if (success) MapMyShotsColors.success else MapMyShotsColors.warning
        )
    }
}

@Preview
@Composable
private fun StatusBadgePreview() {
    MaterialTheme {
        StatusBadge(text = "No location")
    }
}
