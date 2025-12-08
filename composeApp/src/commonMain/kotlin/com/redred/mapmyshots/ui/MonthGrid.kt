package com.redred.mapmyshots.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset

@Composable
fun MonthGrid(
    month: String,
    photos: List<Asset>,
    onTap: (Asset) -> Unit,
    spacing: Dp = 8.dp
) {
    Column {
        Text(month)
        Spacer(Modifier.height(8.dp))

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cols = when {
                maxWidth < 400.dp -> 3
                maxWidth < 600.dp -> 4
                else -> 6
            }
            val cell = (maxWidth - spacing * (cols)) / cols

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                photos.forEach { a ->
                    AssetThumbnail(
                        asset = a,
                        modifier = Modifier
                            .size(cell)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTap(a) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}