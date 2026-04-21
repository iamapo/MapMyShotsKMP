package com.redred.mapmyshots.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.util.groupByMonth
import com.redred.mapmyshots.viewmodel.LoadProgress
import com.redred.mapmyshots.viewmodel.PhotoListEvent
import com.redred.mapmyshots.viewmodel.PhotoListIntent
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    onOpen: (Asset) -> Unit,
    vm: PhotoListViewModel = koinInject(),
    listState: LazyListState = rememberLazyListState(),
    clearOnDispose: Boolean = true
) {
    DisposableEffect(vm, clearOnDispose) {
        onDispose {
            if (clearOnDispose) vm.clear()
        }
    }
    LaunchedEffect(Unit) { vm.onIntent(PhotoListIntent.LoadFirstPage) }

    val uiState by vm.uiState.collectAsState()

    val photos = uiState.photos
    val isLoading = uiState.isLoading
    val isLoadingMore = uiState.isLoadingMore
    val progress = uiState.progress
    val grouped = if (isLoading && photos.isEmpty()) emptyMap() else groupByMonth(photos)

    var pendingDelete by remember { mutableStateOf<Asset?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                PhotoListEvent.DeleteFailed ->
                    deleteError = "Löschen fehlgeschlagen (ggf. fehlende Berechtigung)."
            }
        }
    }

    PhotoListScreenContent(
        listState = listState,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        progress = progress,
        grouped = grouped,
        onOpen = onOpen,
        onLoadMore = { vm.onIntent(PhotoListIntent.LoadNextPage) },
        onLongPress = { asset -> pendingDelete = asset }
    )

    if (pendingDelete != null) {
        val asset = pendingDelete!!
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Foto löschen?") },
            text = { Text("Möchtest du dieses Foto wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        vm.onIntent(PhotoListIntent.Delete(asset))
                    }
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Abbrechen") }
            }
        )
    }

    if (deleteError != null) {
        AlertDialog(
            onDismissRequest = { deleteError = null },
            title = { Text("Fehler") },
            text = { Text(deleteError!!) },
            confirmButton = {
                TextButton(onClick = { deleteError = null }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListScreenContent(
    listState: LazyListState,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    progress: LoadProgress,
    grouped: Map<String, List<Asset>>,
    onOpen: (Asset) -> Unit,
    onLoadMore: () -> Unit,
    onLongPress: (Asset) -> Unit
) {
    val latestLoading by rememberUpdatedState(isLoading)
    val latestLoadingMore by rememberUpdatedState(isLoadingMore)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
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
        color = Color.White
    ) {

        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                Surface(tonalElevation = 2.dp,
                    color = Color.White) {
                    Column(

                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Treffer: ${progress.found} …",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        ) { p ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(p)
            ) {

                if (isLoading && grouped.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                    return@Scaffold
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                ) {
                    grouped.entries.forEach { e ->
                        item(key = e.key) {
                            MonthGrid(
                                month = e.key,
                                photos = e.value,
                                onTap = onOpen,
                                onLongPress = onLongPress
                            )
                        }
                    }

                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    }
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
        ),
        Asset(
            id = "preview_3",
            displayName = "IMG_003",
            takenAt = Instant.fromEpochMilliseconds(1761300000000),
            uri = "content://preview/3"
        ),
        Asset(
            id = "preview_4",
            displayName = "IMG_004",
            takenAt = Instant.fromEpochMilliseconds(1761213600000),
            uri = "content://preview/4"
        )
    )

    PhotoListScreenContent(
        listState = rememberLazyListState(),
        isLoading = false,
        isLoadingMore = false,
        progress = LoadProgress(scanned = 120, total = 120, found = assets.size, active = false),
        grouped = mapOf("October 2025" to assets),
        onOpen = {},
        onLoadMore = {},
        onLongPress = {}
    )
}
