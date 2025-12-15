package com.redred.mapmyshots.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.util.groupByMonth
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    onOpen: (Asset) -> Unit,
    vm: PhotoListViewModel = koinInject()
) {
    DisposableEffect(vm) { onDispose { vm.clear() } }
    LaunchedEffect(Unit) { vm.loadFirstPage() }

    val isLoading by vm.isLoading.collectAsState()
    val isLoadingMore by vm.isLoadingMore.collectAsState()
    val progress by vm.progress.collectAsState()

    val photos by vm.photos.collectAsState()
    val grouped = if (isLoading && photos.isEmpty()) emptyMap() else groupByMonth(photos)

    PhotoListScreenContent(
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        progress = progress,
        grouped = grouped,
        onOpen = onOpen,
        onLoadMore = { vm.loadNextPage() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListScreenContent(
    isLoading: Boolean,
    isLoadingMore: Boolean,
    progress: com.redred.mapmyshots.viewmodel.LoadProgress,
    grouped: Map<String, List<Asset>>,
    onOpen: (Asset) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

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
                            MonthGrid(month = e.key, photos = e.value, onTap = onOpen)
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