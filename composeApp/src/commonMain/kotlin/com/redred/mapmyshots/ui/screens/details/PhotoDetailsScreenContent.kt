package com.redred.mapmyshots.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.ui.components.AssetThumbnailWithDateTime
import com.redred.mapmyshots.ui.components.DetailTopBar
import com.redred.mapmyshots.ui.components.DetailsLoadingState
import com.redred.mapmyshots.ui.components.MetadataCard
import com.redred.mapmyshots.ui.components.SimilarPhotosEmptyState
import com.redred.mapmyshots.ui.components.StatusBadge
import com.redred.mapmyshots.ui.components.SuggestionCard
import com.redred.mapmyshots.ui.components.TimeWindowSelector
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.missing_location_badge
import mapmyshots.composeapp.generated.resources.similar_photos_title
import mapmyshots.composeapp.generated.resources.unknown_location
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
internal fun PhotoDetailsScreenContent(
    photo: Asset,
    timeWindow: TimeWindow,
    loading: Boolean,
    similar: List<Asset>,
    names: Map<String, String>,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    onAssetClicked: (Asset) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MapMyShotsColors.background),
        contentPadding = PaddingValues(MapMyShotsSpacing.screen),
        verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.screen - MapMyShotsSpacing.xxs)
    ) {
        item {
            DetailTopBar(
                onBack = onBack,
                onDelete = onDelete
            )
        }

        item {
            Box {
                AssetThumbnailWithDateTime(
                    asset = photo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MapMyShotsSizes.detailHeroHeight)
                        .clip(MapMyShotsShapes.hero)
                )

                StatusBadge(
                    text = stringResource(Res.string.missing_location_badge),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(MapMyShotsSpacing.xl)
                )
            }
        }

        item {
            MetadataCard(photo = photo)
        }

        item {
            Text(
                text = stringResource(Res.string.similar_photos_title),
                fontSize = MapMyShotsTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = MapMyShotsColors.textPrimary
            )
        }

        item {
            TimeWindowSelector(
                selected = timeWindow,
                onSelected = onTimeWindowSelected
            )
        }

        if (loading) {
            item {
                DetailsLoadingState()
            }
        } else if (similar.isNotEmpty()) {
            items(similar, key = { it.id }) { asset ->
                SuggestionCard(
                    photo = photo,
                    suggestion = asset,
                    place = names[asset.id].orEmpty().ifBlank {
                        asset.displayName ?: stringResource(Res.string.unknown_location)
                    },
                    onClick = { onAssetClicked(asset) }
                )
            }
        } else {
            item {
                SimilarPhotosEmptyState()
            }
        }

        item {
            Spacer(Modifier.height(MapMyShotsSpacing.bottomSpacer))
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun PhotoDetailsScreenContentPreview() {
    val current = Asset(
        id = "preview_current",
        displayName = "IMG_CURRENT",
        takenAt = Instant.fromEpochMilliseconds(1761472800000),
        uri = "content://preview/current"
    )
    val similar = listOf(
        Asset(
            id = "preview_sim_1",
            displayName = "IMG_SIM_1",
            takenAt = Instant.fromEpochMilliseconds(1761469200000),
            uri = "content://preview/sim_1"
        ),
        Asset(
            id = "preview_sim_2",
            displayName = "IMG_SIM_2",
            takenAt = Instant.fromEpochMilliseconds(1761465600000),
            uri = "content://preview/sim_2"
        )
    )

    MaterialTheme {
        PhotoDetailsScreenContent(
            photo = current,
            timeWindow = TimeWindow.OneHour,
            loading = false,
            similar = similar,
            names = mapOf(
                "preview_sim_1" to "Berlin, Germany",
                "preview_sim_2" to "Potsdam, Germany"
            ),
            onTimeWindowSelected = {},
            onAssetClicked = {},
            onDelete = {},
            onBack = {}
        )
    }
}
