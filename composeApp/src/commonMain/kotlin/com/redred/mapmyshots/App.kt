package com.redred.mapmyshots

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.PhotoDetailsScreen
import com.redred.mapmyshots.ui.PhotoListScreen
import com.redred.mapmyshots.ui.theme.MapMyShotsTheme
import com.redred.mapmyshots.viewmodel.PhotoListIntent
import com.redred.mapmyshots.viewmodel.PhotoListTab
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import mapmyshots.composeapp.generated.resources.Res
import mapmyshots.composeapp.generated.resources.bottom_nav_ignored
import mapmyshots.composeapp.generated.resources.bottom_nav_review
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp

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
            Scaffold(
                bottomBar = {
                    if (listUiState.ignoredPhotos.isNotEmpty()) {
                        NavigationBar(
                            modifier = Modifier.height(64.dp),
                            containerColor = Color.White,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            NavigationBarItem(
                                selected = listUiState.selectedTab == PhotoListTab.Review,
                                onClick = { listVm.onIntent(PhotoListIntent.SelectTab(PhotoListTab.Review)) },
                                icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                                label = { Text(stringResource(Res.string.bottom_nav_review)) },
                                alwaysShowLabel = false
                            )
                            NavigationBarItem(
                                selected = listUiState.selectedTab == PhotoListTab.Ignored,
                                onClick = { listVm.onIntent(PhotoListIntent.SelectTab(PhotoListTab.Ignored)) },
                                icon = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
                                label = { Text(stringResource(Res.string.bottom_nav_ignored)) },
                                alwaysShowLabel = false
                            )
                        }
                    }
                }
            ) { innerPadding ->
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
