package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsShapes
import com.redred.mapmyshots.ui.theme.MapMyShotsSpacing
import com.redred.mapmyshots.ui.theme.MapMyShotsStroke
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply_location_success
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SuccessBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = MapMyShotsStroke.thin,
                color = MapMyShotsColors.successBorder,
                shape = MapMyShotsShapes.card
            )
            .background(
                color = MapMyShotsColors.successBackground,
                shape = MapMyShotsShapes.card
            )
            .padding(horizontal = MapMyShotsSpacing.xl, vertical = MapMyShotsSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircleOutline,
            contentDescription = null,
            tint = MapMyShotsColors.success
        )
        Text(
            text = text,
            color = MapMyShotsColors.success,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun SuccessBannerPreview() {
    MaterialTheme {
        SuccessBanner(text = stringResource(Res.string.apply_location_success))
    }
}
