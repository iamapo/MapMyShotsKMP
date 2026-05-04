package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun IconTextButton(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(MapMyShotsSizes.iconButton)
            .clip(CircleShape)
            .background(MapMyShotsColors.transparent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MapMyShotsColors.primary
        )
    }
}

@Preview
@Composable
private fun IconTextButtonPreview() {
    MaterialTheme {
        IconTextButton(Icons.Filled.Search)
    }
}
