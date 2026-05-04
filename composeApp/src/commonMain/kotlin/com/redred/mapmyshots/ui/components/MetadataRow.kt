package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*

@Composable
internal fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MapMyShotsSizes.metadataRowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.width(MapMyShotsSizes.metadataIconWidth),
            tint = MapMyShotsColors.textSecondary
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = MapMyShotsTypography.metadata,
            color = MapMyShotsColors.textSecondary
        )

        Text(
            text = value,
            fontSize = MapMyShotsTypography.metadata,
            color = MapMyShotsColors.textValue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun MetadataRowPreview() {
    MaterialTheme {
        MetadataRow(
            icon = Icons.Filled.AccessTime,
            label = "Time",
            value = "14:32"
        )
    }
}
