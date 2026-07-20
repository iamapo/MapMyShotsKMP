package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsShapes
import com.redred.mapmyshots.ui.theme.MapMyShotsSpacing
import com.redred.mapmyshots.ui.theme.MapMyShotsStroke
import com.redred.mapmyshots.ui.theme.MapMyShotsTypography
import com.redred.mapmyshots.viewmodel.LoadProgress
import com.redred.mapmyshots.viewmodel.PhotoListTab
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.bottom_nav_ignored
import mapmyshots.composeapp.generated.resources.bottom_nav_review
import mapmyshots.composeapp.generated.resources.ignored_photos_count
import mapmyshots.composeapp.generated.resources.photos_without_location_count
import mapmyshots.composeapp.generated.resources.photos_without_location_count_loading
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GalleryHeader(
    selectedTab: PhotoListTab,
    reviewCount: Int,
    ignoredCount: Int,
    progress: LoadProgress,
    onSelectTab: (PhotoListTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MapMyShotsSpacing.md),
    ) {
        Column {
            Text(
                text = stringResource(
                    if (selectedTab == PhotoListTab.Review) {
                        Res.string.bottom_nav_review
                    } else {
                        Res.string.bottom_nav_ignored
                    }
                ),
                fontSize = MapMyShotsTypography.heroTitle,
                fontWeight = FontWeight.Bold,
                color = MapMyShotsColors.textPrimary
            )

            Spacer(Modifier.height(MapMyShotsSpacing.xxs))

            Text(
                text = if (selectedTab == PhotoListTab.Review && progress.active) {
                    stringResource(Res.string.photos_without_location_count_loading, reviewCount)
                } else if (selectedTab == PhotoListTab.Review) {
                    stringResource(Res.string.photos_without_location_count, reviewCount)
                } else {
                    stringResource(Res.string.ignored_photos_count, ignoredCount)
                },
                fontSize = MapMyShotsTypography.gallerySubtitle,
                color = MapMyShotsColors.textMuted
            )
        }

        if (ignoredCount > 0) {
            Spacer(Modifier.height(MapMyShotsSpacing.screen))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MapMyShotsShapes.pill)
                    .background(MapMyShotsColors.surfaceMuted)
                    .border(MapMyShotsStroke.thin, MapMyShotsColors.border, MapMyShotsShapes.pill)
                    .padding(MapMyShotsSpacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xxs)
            ) {
                HeaderTabChip(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.bottom_nav_review),
                    selected = selectedTab == PhotoListTab.Review,
                    onClick = { onSelectTab(PhotoListTab.Review) }
                )
                HeaderTabChip(
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.bottom_nav_ignored),
                    selected = selectedTab == PhotoListTab.Ignored,
                    onClick = { onSelectTab(PhotoListTab.Ignored) }
                )
            }
        }
    }
}

@Composable
private fun HeaderTabChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MapMyShotsColors.surface else MapMyShotsColors.transparent
    val textColor = if (selected) MapMyShotsColors.primary else MapMyShotsColors.textValue
    Box(
        modifier = modifier
            .clip(MapMyShotsShapes.pill)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = MapMyShotsSpacing.lg, vertical = MapMyShotsSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = MapMyShotsTypography.suggestionTitle,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun GalleryHeaderPreview() {
    MaterialTheme {
        GalleryHeader(
            selectedTab = PhotoListTab.Review,
            reviewCount = 24,
            ignoredCount = 8,
            progress = LoadProgress(scanned = 120, found = 24, active = false),
            onSelectTab = {}
        )
    }
}
