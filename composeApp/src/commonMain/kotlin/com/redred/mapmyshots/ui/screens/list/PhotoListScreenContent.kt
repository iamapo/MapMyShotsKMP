package com.redred.mapmyshots.ui.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.components.FullScreenLoadingState
import com.redred.mapmyshots.ui.components.GalleryHeader
import com.redred.mapmyshots.ui.components.InlineLoadingState
import com.redred.mapmyshots.ui.components.PhotoGridCard
import com.redred.mapmyshots.ui.theme.*
import com.redred.mapmyshots.viewmodel.LoadProgress
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
internal fun PhotoListScreenContent(
    listState: LazyListState,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    progress: LoadProgress,
    photos: List<Asset>,
    onOpen: (Asset) -> Unit,
    onLoadMore: () -> Unit,
    onLongPress: (Asset) -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
    val retainedListState = listState
    val gridState = rememberLazyGridState()
    val latestLoading by rememberUpdatedState(isLoading)
    val latestLoadingMore by rememberUpdatedState(isLoadingMore)
    val visiblePhotos = remember(photos) { photos }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .map { info ->
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                last to info.totalItemsCount
            }
            .distinctUntilChanged()
            .collect { (lastVisible, totalItems) ->
                if (!latestLoading && !latestLoadingMore && totalItems > 0 && lastVisible >= totalItems - 2) {
                    onLoadMore()
                }
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MapMyShotsColors.background
    ) {
        if (isLoading && visiblePhotos.isEmpty()) {
            FullScreenLoadingState()
            return@Surface
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = MapMyShotsSizes.galleryMinCell),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MapMyShotsSpacing.screen),
            horizontalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MapMyShotsSpacing.xl)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GalleryHeader(
                    count = if (progress.found > visiblePhotos.size) progress.found else visiblePhotos.size,
                    progress = progress
                )
            }

            items(visiblePhotos, key = { it.id }) { photo ->
                PhotoGridCard(
                    photo = photo,
                    onClick = { onOpen(photo) },
                    onLongClick = { onLongPress(photo) }
                )
            }

            if (isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    InlineLoadingState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MapMyShotsSpacing.xxl)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun PhotoListScreenContentPreview() {
    val assets = listOf(
        Asset(
            id = "preview_1",
            displayName = "IMG_001",
            takenAt = Instant.fromEpochMilliseconds(1761472800000),
            uri = "content://preview/1"
        ),
        Asset(
            id = "preview_2",
            displayName = "IMG_002",
            takenAt = Instant.fromEpochMilliseconds(1761386400000),
            uri = "content://preview/2"
        )
    )

    MaterialTheme {
        PhotoListScreenContent(
            listState = rememberLazyListState(),
            isLoading = false,
            isLoadingMore = false,
            progress = LoadProgress(scanned = 120, total = 120, found = assets.size, active = false),
            photos = assets,
            onOpen = {},
            onLoadMore = {},
            onLongPress = {}
        )
    }
}
