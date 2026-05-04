package com.redred.mapmyshots.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun DividerLight() {
    HorizontalDivider(
        color = MapMyShotsColors.divider,
        thickness = MapMyShotsStroke.thin
    )
}

@Preview
@Composable
private fun DividerLightPreview() {
    MaterialTheme {
        DividerLight()
    }
}
