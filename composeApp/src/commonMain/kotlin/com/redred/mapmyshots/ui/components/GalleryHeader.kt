package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.*
import com.redred.mapmyshots.viewmodel.LoadProgress
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.photos_without_location_count
import mapmyshots.composeapp.generated.resources.photos_without_location_count_loading
import mapmyshots.composeapp.generated.resources.photos_without_location_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GalleryHeader(
    count: Int,
    progress: LoadProgress
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MapMyShotsSpacing.xxs),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.photos_without_location_title),
                fontSize = MapMyShotsTypography.heroTitle,
                fontWeight = FontWeight.Bold,
                color = MapMyShotsColors.textPrimary
            )

            Spacer(Modifier.height(MapMyShotsSpacing.xxs))

            Text(
                text = if (progress.active) {
                    stringResource(Res.string.photos_without_location_count_loading, count)
                } else {
                    stringResource(Res.string.photos_without_location_count, count)
                },
                fontSize = MapMyShotsTypography.gallerySubtitle,
                color = MapMyShotsColors.textMuted
            )
        }

        IconTextButton(Icons.Filled.Search)
        Spacer(Modifier.width(MapMyShotsSpacing.md))
        IconTextButton(Icons.Filled.ViewModule)
    }
}

@Preview
@Composable
private fun GalleryHeaderPreview() {
    MaterialTheme {
        GalleryHeader(
            count = 14,
            progress = LoadProgress(found = 14, active = false)
        )
    }
}
