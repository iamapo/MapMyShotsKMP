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
import com.redred.mapmyshots.viewmodel.PhotoListTab
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.ignored_photos_count
import mapmyshots.composeapp.generated.resources.ignored_photos_title
import mapmyshots.composeapp.generated.resources.photos_without_location_count
import mapmyshots.composeapp.generated.resources.photos_without_location_count_loading
import mapmyshots.composeapp.generated.resources.photos_without_location_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GalleryHeader(
    selectedTab: PhotoListTab,
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
                text = stringResource(
                    if (selectedTab == PhotoListTab.Review) {
                        Res.string.photos_without_location_title
                    } else {
                        Res.string.ignored_photos_title
                    }
                ),
                fontSize = MapMyShotsTypography.heroTitle,
                fontWeight = FontWeight.Bold,
                color = MapMyShotsColors.textPrimary
            )

            Spacer(Modifier.height(MapMyShotsSpacing.xxs))

            Text(
                text = if (selectedTab == PhotoListTab.Review && progress.active) {
                    stringResource(Res.string.photos_without_location_count_loading, count)
                } else if (selectedTab == PhotoListTab.Review) {
                    stringResource(Res.string.photos_without_location_count, count)
                } else {
                    stringResource(Res.string.ignored_photos_count, count)
                },
                fontSize = MapMyShotsTypography.gallerySubtitle,
                color = MapMyShotsColors.textMuted
            )
        }
    }
}

@Preview
@Composable
private fun GalleryHeaderPreview() {
    MaterialTheme {
        GalleryHeader(
            selectedTab = PhotoListTab.Review,
            count = 14,
            progress = LoadProgress(found = 14, active = false)
        )
    }
}
