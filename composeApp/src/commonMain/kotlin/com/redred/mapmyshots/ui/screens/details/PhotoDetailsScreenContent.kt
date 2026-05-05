package com.redred.mapmyshots.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.ui.components.AssetThumbnailWithDateTime
import com.redred.mapmyshots.ui.components.DetailTopBar
import com.redred.mapmyshots.ui.components.DetailsLoadingState
import com.redred.mapmyshots.ui.components.MetadataCard
import com.redred.mapmyshots.ui.components.SimilarPhotosEmptyState
import com.redred.mapmyshots.ui.components.StatusBadge
import com.redred.mapmyshots.ui.components.SuggestionCard
import com.redred.mapmyshots.ui.components.SuccessBanner
import com.redred.mapmyshots.ui.components.TimeWindowSelector
import com.redred.mapmyshots.ui.theme.*
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply_location_success
import mapmyshots.composeapp.generated.resources.choose_location
import mapmyshots.composeapp.generated.resources.location_set_badge
import mapmyshots.composeapp.generated.resources.missing_location_badge
import mapmyshots.composeapp.generated.resources.similar_photos_title
import mapmyshots.composeapp.generated.resources.unknown_location
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
internal fun PhotoDetailsScreenContent(
    photo: Asset,
    isIgnored: Boolean,
    timeWindow: TimeWindow,
    loading: Boolean,
    similar: List<Asset>,
    names: Map<String, String>,
    hasLocation: Boolean,
    currentLocationName: String?,
    showApplySuccess: Boolean,
    appliedSuggestionId: String?,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    onAssetClicked: (Asset) -> Unit,
    onChooseLocationOnMap: () -> Unit,
    onDelete: () -> Unit,
    onToggleIgnored: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MapMyShotsColors.background)
            .pointerInput(onBack) {
                val edgeWidth = 28.dp.toPx()
                val triggerDistance = 72.dp.toPx()

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.position.x > edgeWidth) return@awaitEachGesture

                    var totalDx = 0f
                    var totalDy = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) break

                        val delta = change.position - change.previousPosition
                        totalDx += delta.x
                        totalDy += delta.y

                        if (abs(totalDy) > abs(totalDx) && abs(totalDy) > triggerDistance / 2f) {
                            break
                        }

                        if (totalDx > triggerDistance && abs(totalDx) > abs(totalDy)) {
                            change.consume()
                            onBack()
                            break
                        }

                        if (totalDx > 0f && abs(totalDx) > abs(totalDy)) {
                            change.consume()
                        }
                    }
                }
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize(),
            contentPadding = PaddingValues(MapMyShotsSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.screen - MapMyShotsSpacing.xxs)
        ) {
            item {
                DetailTopBar(
                    onBack = onBack,
                    onDelete = onDelete,
                    isIgnored = isIgnored,
                    onToggleIgnored = onToggleIgnored
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
                            .shadow(
                                elevation = 4.dp,
                                shape = MapMyShotsShapes.card
                            )
                    )

                    StatusBadge(
                        text = stringResource(
                            if (hasLocation) Res.string.location_set_badge else Res.string.missing_location_badge
                        ),
                        success = hasLocation,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(MapMyShotsSpacing.xl)
                    )
                }
            }

            if (showApplySuccess) {
                item {
                    SuccessBanner(text = stringResource(Res.string.apply_location_success))
                }
            }

            item {
                MetadataCard(
                    photo = photo,
                    locationName = currentLocationName
                )
            }

            item {
                Button(
                    onClick = onChooseLocationOnMap,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MapMyShotsColors.primary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(Res.string.choose_location),
                        modifier = Modifier.padding(start = MapMyShotsSpacing.sm)
                    )
                }
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
                        isApplied = appliedSuggestionId == asset.id,
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
            isIgnored = false,
            timeWindow = TimeWindow.OneHour,
            loading = false,
            similar = similar,
            names = mapOf(
                "preview_sim_1" to "Berlin, Germany",
                "preview_sim_2" to "Potsdam, Germany"
            ),
            hasLocation = false,
            currentLocationName = "",
            showApplySuccess = false,
            appliedSuggestionId = "",
            onTimeWindowSelected = {},
            onAssetClicked = {},
            onChooseLocationOnMap = {},
            onDelete = {},
            onToggleIgnored = {},
            onBack = {}
        )
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun PhotoDetailsScreenContentNewLocationPreview() {
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
            isIgnored = false,
            timeWindow = TimeWindow.OneHour,
            loading = false,
            similar = similar,
            names = mapOf(
                "preview_sim_1" to "Berlin, Germany",
                "preview_sim_2" to "Potsdam, Germany"
            ),
            hasLocation = true,
            currentLocationName = "Berlin, Germany",
            showApplySuccess = true,
            appliedSuggestionId = "preview_sim_1",
            onTimeWindowSelected = {},
            onAssetClicked = {},
            onChooseLocationOnMap = {},
            onDelete = {},
            onToggleIgnored = {},
            onBack = {}
        )
    }
}
