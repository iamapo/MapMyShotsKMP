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
import com.redred.mapmyshots.ui.components.LocationMapCandidate
import com.redred.mapmyshots.ui.components.LocationMapPickerDialog
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
    isIgnored: Boolean,
    onLocationApplied: () -> Unit,
    onDeleted: () -> Unit,
    onToggleIgnored: (Asset, Boolean) -> Unit,
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
    var showLocationPicker by remember { mutableStateOf(false) }
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
                is PhotoDetailsEvent.Saved -> {
                    val source = applyingFrom
                    hasLocation = true
                    showApplySuccess = true
                    appliedSuggestionId = source?.id
                    currentLocationName = event.locationName
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
        isIgnored = isIgnored,
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
        onChooseLocationOnMap = {
            showLocationPicker = true
        },
        onDelete = {
            pendingDelete = true
        },
        onToggleIgnored = {
            onToggleIgnored(photo, isIgnored)
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

    if (showLocationPicker) {
        val fallbackLocation = uiState.suggestionLocations.values.firstOrNull() ?: (52.5200 to 13.4050)
        val candidates = uiState.similar.mapNotNull { asset ->
            val location = uiState.suggestionLocations[asset.id] ?: return@mapNotNull null
            LocationMapCandidate(
                label = names[asset.id].orEmpty().ifBlank { asset.displayName ?: asset.id },
                lat = location.first,
                lon = location.second
            )
        }

        LocationMapPickerDialog(
            initialLocation = uiState.currentLocation ?: fallbackLocation,
            candidates = candidates,
            onDismiss = { showLocationPicker = false },
            onApply = { lat, lon ->
                showLocationPicker = false
                applyError = null
                showApplySuccess = false
                applyingFrom = null
                vm.onIntent(PhotoDetailsIntent.ApplyManualLocation(lat, lon))
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
