package com.redred.mapmyshots.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.ui.theme.MapMyShotsColors
import com.redred.mapmyshots.ui.theme.MapMyShotsShapes
import com.redred.mapmyshots.ui.theme.MapMyShotsSizes
import com.redred.mapmyshots.ui.theme.MapMyShotsSpacing
import com.redred.mapmyshots.ui.theme.MapMyShotsStroke
import com.redred.mapmyshots.ui.theme.MapMyShotsTypography
import com.redred.mapmyshots.viewmodel.LoadProgress
import com.redred.mapmyshots.viewmodel.PhotoListTab
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.bottom_nav_ignored
import mapmyshots.composeapp.generated.resources.bottom_nav_review
import mapmyshots.composeapp.generated.resources.gallery_stat_ignored
import mapmyshots.composeapp.generated.resources.gallery_stat_open
import mapmyshots.composeapp.generated.resources.gallery_stat_scanned
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

        Spacer(Modifier.height(MapMyShotsSpacing.xxl))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xl)
        ) {
            StatCard(value = progress.scanned, label = stringResource(Res.string.gallery_stat_scanned))
            StatCard(value = reviewCount, label = stringResource(Res.string.gallery_stat_open))
            if (ignoredCount > 0) {
                StatCard(value = ignoredCount, label = stringResource(Res.string.gallery_stat_ignored))
            }
        }

        Spacer(Modifier.height(MapMyShotsSpacing.xxl))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.md)
        ) {

            if (ignoredCount > 0) {
                HeaderTabChip(
                    label = stringResource(Res.string.bottom_nav_review),
                    selected = selectedTab == PhotoListTab.Review,
                    onClick = { onSelectTab(PhotoListTab.Review) }
                )
                HeaderTabChip(
                    label = stringResource(Res.string.bottom_nav_ignored),
                    selected = selectedTab == PhotoListTab.Ignored,
                    onClick = { onSelectTab(PhotoListTab.Ignored) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String
) {
    Surface(
        shape = MapMyShotsShapes.hero,
        color = MapMyShotsColors.surface,
        border = BorderStroke(MapMyShotsStroke.thin, MapMyShotsColors.border),
        modifier = Modifier.width(MapMyShotsSizes.headerStatCardWidth)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = MapMyShotsSpacing.xxl, vertical = MapMyShotsSpacing.xl)
        ) {
            Text(
                text = value.toString(),
                fontSize = MapMyShotsTypography.statValue,
                fontWeight = FontWeight.Bold,
                color = MapMyShotsColors.textPrimary
            )

            Text(
                text = label,
                fontSize = MapMyShotsTypography.suggestionTitle,
                color = MapMyShotsColors.textMuted
            )
        }
    }
}

@Composable
private fun HeaderTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) MapMyShotsColors.primary else MapMyShotsColors.surface
    val textColor = if (selected) MapMyShotsColors.onImage else MapMyShotsColors.textValue
    Box(
        modifier = Modifier
            .clip(MapMyShotsShapes.pill)
            .background(background)
            .border(
                width = MapMyShotsStroke.thin,
                color = if (selected) MapMyShotsColors.primary else MapMyShotsColors.border,
                shape = MapMyShotsShapes.pill
            )
            .clickable(onClick = onClick)
            .padding(horizontal = MapMyShotsSpacing.lg, vertical = MapMyShotsSpacing.xxxs)
    ) {
        Text(
            text = label,
            fontSize = MapMyShotsTypography.suggestionTitle,
            fontWeight = FontWeight.Normal,
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
