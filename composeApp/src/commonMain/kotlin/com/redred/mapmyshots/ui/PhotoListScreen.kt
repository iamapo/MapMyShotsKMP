package com.redred.mapmyshots.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(onOpen: (Asset) -> Unit, vm: PhotoListViewModel = koinInject()) {

    DisposableEffect(vm) {
        onDispose { vm.clear() }
    }

    val isLoading = true
    LaunchedEffect(Unit) { vm.load() }

    val grouped = if (isLoading) emptyMap() else vm.groupedByMonth()

    PhotoListScreenContent(
        isLoading = isLoading,
        grouped = grouped,
        onOpen = onOpen
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoListScreenContent(
    isLoading: Boolean,
    grouped: Map<String, List<Asset>>,
    onOpen: (Asset) -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("MapMyShot") }) }) { p ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(p)) {
                CircularProgressIndicator()
            }
        }/* else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(p)
                    .padding(12.dp)
            ) {
                items(grouped.entries.toList(), key = { it.key }) { e ->
                    MonthGrid(month = e.key, photos = e.value, onTap = onOpen)
                }
            }
        }*/
    }
}