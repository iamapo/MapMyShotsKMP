package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply
import mapmyshots.composeapp.generated.resources.applied
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SuggestionApplyAction(
    applied: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(horizontal = MapMyShotsSpacing.lg)
            .width(MapMyShotsSizes.suggestionActionWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(MapMyShotsSizes.successIcon)
                .clip(CircleShape)
                .then(
                    if (applied) {
                        Modifier.background(
                            color = MapMyShotsColors.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = MapMyShotsStroke.medium,
                            color = MapMyShotsColors.primary,
                            shape = CircleShape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (applied) Icons.Filled.Check else Icons.Filled.NorthEast,
                contentDescription = null,
                tint = if (applied) MapMyShotsColors.surface else MapMyShotsColors.primary
            )
        }

        Spacer(Modifier.height(MapMyShotsSpacing.xs))

        Text(
            text = stringResource(if (applied) Res.string.applied else Res.string.apply),
            fontSize = MapMyShotsTypography.caption,
            color = MapMyShotsColors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun SuggestionApplyActionPreview() {
    MaterialTheme {
        SuggestionApplyAction()
    }
}
