package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.metadata_date
import mapmyshots.composeapp.generated.resources.metadata_file
import mapmyshots.composeapp.generated.resources.metadata_time
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MetadataCard(photo: Asset) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MapMyShotsShapes.metadataCard,
        colors = CardDefaults.elevatedCardColors(containerColor = MapMyShotsColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = MapMyShotsSpacing.screen - MapMyShotsSpacing.xxs, vertical = MapMyShotsSpacing.sm)
        ) {
            MetadataRow(
                icon = Icons.Filled.CalendarToday,
                label = stringResource(Res.string.metadata_date),
                value = formatDate(photo.takenAt)
            )

            DividerLight()

            MetadataRow(
                icon = Icons.Filled.AccessTime,
                label = stringResource(Res.string.metadata_time),
                value = formatTime(photo.takenAt)
            )

            DividerLight()

            MetadataRow(
                icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                label = stringResource(Res.string.metadata_file),
                value = photo.displayName ?: photo.id
            )
        }
    }
}

@Preview
@Composable
private fun MetadataCardPreview() {
    MaterialTheme {
        MetadataCard(photo = previewAsset())
    }
}
