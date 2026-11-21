package com.redred.mapmyshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.PhotoDetailsScreen
import com.redred.mapmyshots.ui.PhotoListScreen
import com.redred.mapmyshots.ui.theme.MapMyShotsTheme
import com.redred.mapmyshots.viewmodel.PhotoListIntent
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.compose.getKoin

private const val ROUTE_LIST = "list"
private const val ARG_ASSET_ID = "assetId"
private const val ROUTE_DETAILS = "details/{$ARG_ASSET_ID}"

@Composable
fun App(
    onRequestPermissions: ((onGranted: () -> Unit) -> Unit)? = null
) {
    val koin = getKoin()
    // Scoped to the app-level ViewModelStoreOwner, so the scan survives configuration changes
    // and is shared by both destinations.
    val listVm = viewModel { koin.get<PhotoListViewModel>() }
    val gridState = rememberSaveable(saver = LazyGridState.Saver) { LazyGridState() }
    val navController = rememberNavController()

    LaunchedEffect(Unit) { onRequestPermissions?.invoke(listVm::reload) }

    MapMyShotsTheme {
        NavHost(
            navController = navController,
            startDestination = ROUTE_LIST
        ) {
            composable(ROUTE_LIST) {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PhotoListScreen(
                            onOpen = { asset -> navController.navigate("details/${asset.id}") },
                            vm = listVm,
                            gridState = gridState
                        )
                    }
                }
            }
            composable(
                route = ROUTE_DETAILS,
                arguments = listOf(navArgument(ARG_ASSET_ID) { type = NavType.StringType })
            ) { entry ->
                val assetId = entry.savedStateHandle.get<String>(ARG_ASSET_ID).orEmpty()
                PhotoDetailsDestination(
                    assetId = assetId,
                    listVm = listVm,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun PhotoDetailsDestination(
    assetId: String,
    listVm: PhotoListViewModel,
    navController: NavHostController
) {
    val listUiState by listVm.uiState.collectAsState()
    // Captured on open: the photo leaves the list once its location is applied, but the screen stays.
    val photo: Asset? = remember(assetId) {
        val state = listVm.uiState.value
        (state.reviewPhotos + state.ignoredPhotos).firstOrNull { it.id == assetId }
    }
    val isIgnored = listUiState.ignoredPhotos.any { it.id == assetId }

    // If the photo is unknown (e.g. list rebuilt after process death), go back to the list.
    // shortcut: no lookup via PhotoRepository, add one if details must survive process death.
    if (photo == null) {
        LaunchedEffect(assetId) { navController.popBackStack() }
        return
    }

    PhotoDetailsScreen(
        photo = photo,
        isIgnored = isIgnored,
        onLocationApplied = {
            listVm.onIntent(PhotoListIntent.RemoveFromList(photo.id))
        },
        onDeleted = {
            listVm.onIntent(PhotoListIntent.AssetDeleted(photo.id))
            navController.popBackStack()
        },
        onToggleIgnored = { asset, ignored ->
            listVm.onIntent(
                if (ignored) PhotoListIntent.Restore(asset.id) else PhotoListIntent.Ignore(asset)
            )
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
    )
}
