package com.redred.mapmyshots.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.components.ApplyLocationDialog
import com.redred.mapmyshots.ui.components.ConfirmDeletePhotoDialog
import com.redred.mapmyshots.ui.components.ErrorDialog
import com.redred.mapmyshots.ui.screens.details.PhotoDetailsScreenContent
import com.redred.mapmyshots.viewmodel.PhotoDetailsEvent
import com.redred.mapmyshots.viewmodel.PhotoDetailsIntent
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply_location_failed
import mapmyshots.composeapp.generated.resources.delete_failed
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoDetailsScreen(
    photo: Asset,
    onLocationApplied: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit
) {
    val vm: PhotoDetailsViewModel = koinInject(parameters = { parametersOf(photo) })

    DisposableEffect(vm) {
        onDispose { vm.clear() }
    }

    val uiState by vm.uiState.collectAsState()
    val names = uiState.locationNames
    var pendingApplyFrom by remember { mutableStateOf<Asset?>(null) }
    var applyingFrom by remember { mutableStateOf<Asset?>(null) }
    var pendingDelete by remember { mutableStateOf(false) }
    var applyError by remember { mutableStateOf<PhotoDetailsEvent.Error?>(null) }
    var deleteError by remember { mutableStateOf(false) }
    var hasLocation by remember(photo.id) { mutableStateOf(photo.hasLocation == true) }
    var currentLocationName by remember(photo.id) { mutableStateOf<String?>(null) }
    var showApplySuccess by remember(photo.id) { mutableStateOf(false) }
    var appliedSuggestionId by remember(photo.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(photo.id) {
        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                PhotoDetailsEvent.Saved -> {
                    val source = applyingFrom
                    hasLocation = true
                    showApplySuccess = true
                    appliedSuggestionId = source?.id
                    currentLocationName = source?.let { asset ->
                        names[asset.id].orEmpty().ifBlank { asset.displayName ?: asset.id }
                    }
                    applyingFrom = null
                    onLocationApplied()
                }
                PhotoDetailsEvent.Deleted -> onDeleted()
                PhotoDetailsEvent.DeleteFailed -> {
                    applyingFrom = null
                    deleteError = true
                }
                is PhotoDetailsEvent.Error -> {
                    applyingFrom = null
                    applyError = event
                }
            }
        }
    }

    PhotoDetailsScreenContent(
        photo = photo,
        timeWindow = uiState.timeWindow,
        loading = uiState.loading,
        similar = uiState.similar,
        names = names,
        hasLocation = hasLocation,
        currentLocationName = currentLocationName,
        showApplySuccess = showApplySuccess,
        appliedSuggestionId = appliedSuggestionId,
        onTimeWindowSelected = { timeWindow ->
            vm.onIntent(PhotoDetailsIntent.SetTimeWindow(timeWindow))
        },
        onAssetClicked = { asset ->
            pendingApplyFrom = asset
        },
        onDelete = {
            pendingDelete = true
        },
        onBack = onBack
    )

    if (pendingDelete) {
        ConfirmDeletePhotoDialog(
            onDismiss = { pendingDelete = false },
            onConfirm = {
                pendingDelete = false
                deleteError = false
                vm.onIntent(PhotoDetailsIntent.Delete)
            }
        )
    }

    if (pendingApplyFrom != null) {
        val src = pendingApplyFrom!!
        ApplyLocationDialog(
            sourceName = names[src.id].orEmpty().ifBlank { src.displayName ?: src.id },
            onDismiss = { pendingApplyFrom = null },
            onConfirm = {
                pendingApplyFrom = null
                applyError = null
                showApplySuccess = false
                applyingFrom = src
                vm.onIntent(PhotoDetailsIntent.ApplyLocationFrom(src))
            }
        )
    }

    if (applyError != null) {
        ErrorDialog(
            message = stringResource(Res.string.apply_location_failed),
            onDismiss = { applyError = null }
        )
    }

    if (deleteError) {
        ErrorDialog(
            message = stringResource(Res.string.delete_failed),
            onDismiss = { deleteError = false }
        )
    }
}
