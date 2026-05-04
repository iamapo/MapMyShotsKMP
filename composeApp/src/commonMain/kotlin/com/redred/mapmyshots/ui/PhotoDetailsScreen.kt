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
import com.redred.mapmyshots.ui.components.ErrorDialog
import com.redred.mapmyshots.ui.screens.details.PhotoDetailsScreenContent
import com.redred.mapmyshots.viewmodel.PhotoDetailsEvent
import com.redred.mapmyshots.viewmodel.PhotoDetailsIntent
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.apply_location_failed
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoDetailsScreen(
    photo: Asset,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val vm: PhotoDetailsViewModel = koinInject(parameters = { parametersOf(photo) })

    DisposableEffect(vm) {
        onDispose { vm.clear() }
    }

    val uiState by vm.uiState.collectAsState()
    val names = uiState.locationNames
    var pendingApplyFrom by remember { mutableStateOf<Asset?>(null) }
    var applyError by remember { mutableStateOf<PhotoDetailsEvent.Error?>(null) }

    LaunchedEffect(photo.id) {
        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                PhotoDetailsEvent.Saved -> onSaved()
                is PhotoDetailsEvent.Error -> applyError = event
            }
        }
    }

    PhotoDetailsScreenContent(
        photo = photo,
        timeWindow = uiState.timeWindow,
        loading = uiState.loading,
        similar = uiState.similar,
        names = names,
        onTimeWindowSelected = { timeWindow ->
            vm.onIntent(PhotoDetailsIntent.SetTimeWindow(timeWindow))
        },
        onAssetClicked = { asset ->
            pendingApplyFrom = asset
        },
        onBack = onBack
    )

    if (pendingApplyFrom != null) {
        val src = pendingApplyFrom!!
        ApplyLocationDialog(
            sourceName = names[src.id].orEmpty().ifBlank { src.displayName ?: src.id },
            onDismiss = { pendingApplyFrom = null },
            onConfirm = {
                pendingApplyFrom = null
                applyError = null
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
}
