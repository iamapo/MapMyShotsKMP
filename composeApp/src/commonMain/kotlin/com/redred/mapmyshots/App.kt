package com.redred.mapmyshots

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val listState = rememberLazyListState()

    DisposableEffect(listVm) {
        onDispose { listVm.clear() }
    }

    val current = remember { mutableStateOf<Asset?>(null) }
    val selectedPhoto = current.value
    MapMyShotsTheme {
        if (selectedPhoto == null) {
            PhotoListScreen(
                onOpen = { asset -> current.value = asset },
                vm = listVm,
                listState = listState,
                clearOnDispose = false
            )
        } else {
            PhotoDetailsScreen(
                photo = selectedPhoto,
                onSaved = {
                    listVm.onIntent(PhotoListIntent.RemoveFromList(selectedPhoto.id))
                    current.value = null
                },
                onBack = { current.value = null }
            )
        }
    }
}
