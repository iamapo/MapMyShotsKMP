package com.redred.mapmyshots

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Scaffold
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.PhotoDetailsScreen
import com.redred.mapmyshots.ui.PhotoListScreen
import com.redred.mapmyshots.ui.theme.MapMyShotsTheme
import com.redred.mapmyshots.viewmodel.PhotoListIntent
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.compose.koinInject

@Composable
fun App(
    onRequestPermissions: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) { onRequestPermissions?.invoke() }

    val listVm: PhotoListViewModel = koinInject()
    val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }

    DisposableEffect(listVm) {
        onDispose { listVm.clear() }
    }

    val current = remember { mutableStateOf<Asset?>(null) }
    val selectedPhoto = current.value
    val listUiState by listVm.uiState.collectAsState()
    val ignoredIds = remember(listUiState.ignoredPhotos) { listUiState.ignoredPhotos.mapTo(hashSetOf()) { it.id } }
    MapMyShotsTheme {
        if (selectedPhoto == null) {
            Scaffold { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
                    PhotoListScreen(
                        onOpen = { asset -> current.value = asset },
                        vm = listVm,
                        gridState = gridState,
                        clearOnDispose = false
                    )
                }
            }
        } else {
            PhotoDetailsScreen(
                photo = selectedPhoto,
                isIgnored = selectedPhoto.id in ignoredIds,
                onLocationApplied = {
                    listVm.onIntent(PhotoListIntent.RemoveFromList(selectedPhoto.id))
                },
                onDeleted = {
                    listVm.onIntent(PhotoListIntent.RemoveFromList(selectedPhoto.id))
                    current.value = null
                },
                onToggleIgnored = { asset, ignored ->
                    listVm.onIntent(
                        if (ignored) PhotoListIntent.Restore(asset.id) else PhotoListIntent.Ignore(asset)
                    )
                    current.value = null
                },
                onBack = { current.value = null }
            )
        }
    }
}
