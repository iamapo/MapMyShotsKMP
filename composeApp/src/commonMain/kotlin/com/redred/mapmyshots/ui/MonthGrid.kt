package com.redred.mapmyshots.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun MonthGrid(
    month: String,
    photos: List<Asset>,
    onTap: (Asset) -> Unit,
    onLongPress: (Asset) -> Unit,
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
                    key(a.id) {
                        AssetThumbnail(
                            asset = a,
                            modifier = Modifier
                                .size(cell)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { onTap(a) },
                                    onLongClick = { onLongPress(a) }
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun MonthGridPreview() {
    val photos = listOf(
        Asset(
            id = "preview_grid_1",
            displayName = "IMG_1001",
            takenAt = Instant.fromEpochMilliseconds(1761472800000),
            uri = "content://preview/grid_1"
        ),
        Asset(
            id = "preview_grid_2",
            displayName = "IMG_1002",
            takenAt = Instant.fromEpochMilliseconds(1761469200000),
            uri = "content://preview/grid_2"
        ),
        Asset(
            id = "preview_grid_3",
            displayName = "IMG_1003",
            takenAt = Instant.fromEpochMilliseconds(1761465600000),
            uri = "content://preview/grid_3"
        ),
        Asset(
            id = "preview_grid_4",
            displayName = "IMG_1004",
            takenAt = Instant.fromEpochMilliseconds(1761462000000),
            uri = "content://preview/grid_4"
        )
    )

    MaterialTheme {
        MonthGrid(
            month = "October 2025",
            photos = photos,
            onTap = {},
            onLongPress = {}
        )
    }
}
