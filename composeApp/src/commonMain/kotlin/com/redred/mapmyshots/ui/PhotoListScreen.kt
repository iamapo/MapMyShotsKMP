package com.redred.mapmyshots.ui

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.components.ConfirmDeletePhotoDialog
import com.redred.mapmyshots.ui.components.ErrorDialog
import com.redred.mapmyshots.ui.components.PhotoListAssetActionsDialog
import com.redred.mapmyshots.ui.screens.list.PhotoListScreenContent
import com.redred.mapmyshots.viewmodel.PhotoListEvent
import com.redred.mapmyshots.viewmodel.PhotoListIntent
import com.redred.mapmyshots.viewmodel.PhotoListTab
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.delete_failed
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoListScreen(
    onOpen: (Asset) -> Unit,
    vm: PhotoListViewModel = koinInject(),
    gridState: LazyGridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() },
    clearOnDispose: Boolean = true
) {
    DisposableEffect(vm, clearOnDispose) {
        onDispose {
            if (clearOnDispose) vm.clear()
        }
    }
    LaunchedEffect(Unit) { vm.onIntent(PhotoListIntent.LoadFirstPage) }

    val uiState by vm.uiState.collectAsState()
    var selectedAsset by remember { mutableStateOf<Asset?>(null) }
    var pendingDelete by remember { mutableStateOf<Asset?>(null) }
    var deleteError by remember { mutableStateOf(false) }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                PhotoListEvent.DeleteFailed ->
                    deleteError = true
            }
        }
    }

    PhotoListScreenContent(
        gridState = gridState,
        isLoading = uiState.isLoading,
        isLoadingMore = uiState.isLoadingMore,
        isLoadingIgnored = uiState.isLoadingIgnored,
        progress = uiState.progress,
        selectedTab = uiState.selectedTab,
        reviewPhotos = uiState.reviewPhotos,
        ignoredPhotos = uiState.ignoredPhotos,
        onOpen = onOpen,
        onLoadMore = { vm.onIntent(PhotoListIntent.LoadNextPage) },
        onLongPress = { asset -> selectedAsset = asset }
    )

    if (selectedAsset != null) {
        val asset = selectedAsset!!
        val isIgnored = uiState.selectedTab == PhotoListTab.Ignored
        PhotoListAssetActionsDialog(
            isIgnored = isIgnored,
            onDismiss = { selectedAsset = null },
            onToggleIgnored = {
                selectedAsset = null
                vm.onIntent(
                    if (isIgnored) {
                        PhotoListIntent.Restore(asset.id)
                    } else {
                        PhotoListIntent.Ignore(asset)
                    }
                )
            },
            onDelete = {
                selectedAsset = null
                pendingDelete = asset
            }
        )
    }

    if (pendingDelete != null) {
        val asset = pendingDelete!!
        ConfirmDeletePhotoDialog(
            onDismiss = { pendingDelete = null },
            onConfirm = {
                pendingDelete = null
                vm.onIntent(PhotoListIntent.Delete(asset))
            }
        )
    }

    if (deleteError) {
        ErrorDialog(
            message = stringResource(Res.string.delete_failed),
            onDismiss = { deleteError = false }
        )
    }
}
