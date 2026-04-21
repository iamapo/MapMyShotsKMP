package com.redred.mapmyshots.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.viewmodel.PhotoDetailsEvent
import com.redred.mapmyshots.viewmodel.PhotoDetailsIntent
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
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
    val timeRange = uiState.timeRange
    val loading = uiState.loading
    val similar = uiState.similar
    val names = uiState.locationNames

    var pendingApplyFrom by remember { mutableStateOf<Asset?>(null) }
    var applyError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(photo.id) {
        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
    }

    LaunchedEffect(vm) {
        vm.events.collect { event ->
            when (event) {
                PhotoDetailsEvent.Saved -> onSaved()
                is PhotoDetailsEvent.Error -> applyError = event.message
            }
        }
    }

    PhotoDetailsScreenContent(
        photo = photo,
        timeRange = timeRange,
        loading = loading,
        similar = similar,
        names = names,
        onTimeRangeSelected = { label ->
            vm.onIntent(PhotoDetailsIntent.SetTimeRange(label))
        },
        onAssetClicked = { a ->
            pendingApplyFrom = a
        },
        onBack = onBack
    )
    if (pendingApplyFrom != null) {
        val src = pendingApplyFrom!!

        AlertDialog(
            onDismissRequest = { pendingApplyFrom = null },
            title = { Text("Location übernehmen?") },
            text = {
                Text(
                    "Möchtest du die Location von „${names[src.id].orEmpty().ifBlank { src.displayName ?: src.id }}“ " +
                            "in das aktuelle Foto übernehmen?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingApplyFrom = null
                        applyError = null
                        vm.onIntent(PhotoDetailsIntent.ApplyLocationFrom(src))
                    }
                ) { Text("Übernehmen") }
            },
            dismissButton = {
                TextButton(onClick = { pendingApplyFrom = null }) { Text("Abbrechen") }
            }
        )
    }

    if (applyError != null) {
        AlertDialog(
            onDismissRequest = { applyError = null },
            title = { Text("Fehler") },
            text = { Text(applyError!!) },
            confirmButton = {
                TextButton(onClick = { applyError = null }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PhotoDetailsScreenContent(
    photo: Asset,
    timeRange: String,
    loading: Boolean,
    similar: List<Asset>,
    names: Map<String, String>,
    onTimeRangeSelected: (String) -> Unit,
    onAssetClicked: (Asset) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(p)
                .padding(12.dp)
        ) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AssetThumbnail(
                    asset = photo,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SegmentedButtons(
                    options = listOf("1 hour", "4 hours", "12 hours"),
                    selected = timeRange,
                    onSelected = onTimeRangeSelected
                )
            }

            Spacer(Modifier.height(16.dp))

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (similar.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(similar, key = { it.id }) { a ->
                        Card(
                            Modifier.clickable { onAssetClicked(a) }
                        ) {
                            Column {
                                AssetThumbnail(
                                    asset = a,
                                    modifier = Modifier
                                        .height(160.dp)
                                        .fillMaxWidth()
                                )
                                Text(
                                    text = names[a.id] ?: "",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Text("No similar photos found")
            }
        }
    }
}

@Composable
private fun SegmentedButtons(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach {
            FilterChip(
                selected = it == selected,
                onClick = { onSelected(it) },
                label = { Text(it) }
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun PhotoDetailsScreenContentPreview() {
    val current = Asset(
        id = "preview_current",
        displayName = "IMG_CURRENT",
        takenAt = Instant.fromEpochMilliseconds(1761472800000),
        uri = "content://preview/current"
    )
    val similar = listOf(
        Asset(
            id = "preview_sim_1",
            displayName = "IMG_SIM_1",
            takenAt = Instant.fromEpochMilliseconds(1761469200000),
            uri = "content://preview/sim_1"
        ),
        Asset(
            id = "preview_sim_2",
            displayName = "IMG_SIM_2",
            takenAt = Instant.fromEpochMilliseconds(1761465600000),
            uri = "content://preview/sim_2"
        )
    )

    MaterialTheme {
        PhotoDetailsScreenContent(
            photo = current,
            timeRange = "1 hour",
            loading = false,
            similar = similar,
            names = mapOf(
                "preview_sim_1" to "Berlin, Germany",
                "preview_sim_2" to "Potsdam, Germany"
            ),
            onTimeRangeSelected = {},
            onAssetClicked = {},
            onBack = {}
        )
    }
}
