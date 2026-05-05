package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.time_window_four_hours
import mapmyshots.composeapp.generated.resources.time_window_one_hour
import mapmyshots.composeapp.generated.resources.time_window_twelve_hours
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TimeWindowSelector(
    selected: TimeWindow,
    onSelected: (TimeWindow) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MapMyShotsSizes.timeSelectorHeight)
            .shadow(
                elevation = 4.dp,
                shape = MapMyShotsShapes.card
            )            .clip(MapMyShotsShapes.pill)
            .background(MapMyShotsColors.surface)
            .border(
                width = MapMyShotsStroke.thin,
                color = MapMyShotsColors.border,
                shape = MapMyShotsShapes.pill
            )
            .padding(MapMyShotsSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xxs)
    ) {
        TimeWindow.entries.forEach { item ->
            val isSelected = selected == item

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(MapMyShotsShapes.pill)
                    .background(if (isSelected) MapMyShotsColors.primary else MapMyShotsColors.transparent)
                    .clickable { onSelected(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.labelText(),
                    fontSize = MapMyShotsTypography.cardDate,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MapMyShotsColors.onImage else MapMyShotsColors.textSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimeWindowSelectorPreview() {
    MaterialTheme {
        TimeWindowSelector(
            selected = TimeWindow.FourHours,
            onSelected = {}
        )
    }
}

@Composable
private fun TimeWindow.labelText(): String {
    return when (this) {
        TimeWindow.OneHour -> stringResource(Res.string.time_window_one_hour)
        TimeWindow.FourHours -> stringResource(Res.string.time_window_four_hours)
        TimeWindow.TwelveHours -> stringResource(Res.string.time_window_twelve_hours)
    }
}
