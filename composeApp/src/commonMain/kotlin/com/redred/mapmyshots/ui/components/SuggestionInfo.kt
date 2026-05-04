package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.hours_away
import mapmyshots.composeapp.generated.resources.hours_minutes_away
import mapmyshots.composeapp.generated.resources.minutes_away
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@Composable
internal fun SuggestionInfo(
    photo: Asset,
    suggestion: Asset,
    place: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MapMyShotsColors.textValue
            )

            Spacer(Modifier.width(MapMyShotsSpacing.sm))

            Text(
                text = place,
                fontSize = MapMyShotsTypography.suggestionTitle,
                fontWeight = FontWeight.SemiBold,
                color = MapMyShotsColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(MapMyShotsSpacing.sm))

        Text(
            text = "${formatDate(suggestion.takenAt)} · ${formatTime(suggestion.takenAt)} · ${formatDistanceText(photo, suggestion)}",
            fontSize = MapMyShotsTypography.badge,
            color = MapMyShotsColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun formatDistanceText(photo: Asset, suggestion: Asset): String {
    val minutes = kotlin.math.abs(
        photo.takenAt.toEpochMilliseconds() - suggestion.takenAt.toEpochMilliseconds()
    ) / 60000
    return if (minutes < 60) {
        stringResource(Res.string.minutes_away, minutes.toInt())
    } else {
        val hours = minutes / 60
        val rest = minutes % 60
        if (rest == 0L) {
            stringResource(Res.string.hours_away, hours.toInt())
        } else {
            stringResource(Res.string.hours_minutes_away, hours.toInt(), rest.toInt())
        }
    }
}

@Preview
@Composable
private fun SuggestionInfoPreview() {
    MaterialTheme {
        SuggestionInfo(
            photo = previewAsset(),
            suggestion = previewSuggestionAsset(),
            place = "Munich · Marienplatz"
        )
    }
}
