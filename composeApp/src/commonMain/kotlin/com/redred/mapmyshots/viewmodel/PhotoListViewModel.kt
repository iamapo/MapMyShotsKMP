package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.service.IgnoredPhotoService
import com.redred.mapmyshots.service.PhotoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoadProgress(
    val scanned: Int = 0,
    val total: Int = 0,
    val found: Int = 0,
    val active: Boolean = false
)

enum class PhotoListTab {
    Review,
    Ignored
}

data class PhotoListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingIgnored: Boolean = false,
    val progress: LoadProgress = LoadProgress(),
    val selectedTab: PhotoListTab = PhotoListTab.Review,
    val reviewPhotos: List<Asset> = emptyList(),
    val ignoredPhotos: List<Asset> = emptyList()
) {
    val visiblePhotos: List<Asset>
        get() = when (selectedTab) {
            PhotoListTab.Review -> reviewPhotos
            PhotoListTab.Ignored -> ignoredPhotos
        }
}

sealed interface PhotoListIntent {
    data object LoadFirstPage : PhotoListIntent
    data object LoadNextPage : PhotoListIntent
    data class SelectTab(val tab: PhotoListTab) : PhotoListIntent
    data class Delete(val asset: Asset) : PhotoListIntent
    data class Ignore(val asset: Asset) : PhotoListIntent
    data class Restore(val assetId: String) : PhotoListIntent
    data class RemoveFromList(val assetId: String) : PhotoListIntent
}

sealed interface PhotoListEvent {
    data object DeleteFailed : PhotoListEvent
}

class PhotoListViewModel(
    private val service: PhotoService,
    private val ignoredPhotoService: IgnoredPhotoService
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _uiState = MutableStateFlow(PhotoListUiState())
    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PhotoListEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhotoListEvent> = _events.asSharedFlow()

    private val candidatePageSize = 200
    private var candidateOffset = 0
    private var endReached = false

    private var scannedTotal = 0
    private var foundTotal = 0

    private val minInitialHits = 60
    private var initialized = false
    private var ignoredIds: Set<String> = emptySet()
    private val knownPhotoIds = linkedSetOf<String>()

    fun clear() = job.cancel()

    fun onIntent(intent: PhotoListIntent) {
        when (intent) {
            PhotoListIntent.LoadFirstPage -> loadFirstPage()
            PhotoListIntent.LoadNextPage -> loadNextPage()
            is PhotoListIntent.SelectTab -> selectTab(intent.tab)
            is PhotoListIntent.Delete -> delete(intent.asset)
            is PhotoListIntent.Ignore -> ignore(intent.asset)
            is PhotoListIntent.Restore -> restore(intent.assetId)
            is PhotoListIntent.RemoveFromList -> removeFromList(intent.assetId, clearIgnored = true)
        }
    }

    fun loadFirstPage() {
        if (initialized) return
        initialized = true
        candidateOffset = 0
        endReached = false
        scannedTotal = 0
        foundTotal = 0
        knownPhotoIds.clear()
        _uiState.update { it.copy(reviewPhotos = emptyList(), ignoredPhotos = emptyList()) }

        scope.launch {
            loadIgnoredPhotos()

            var first = true
            while (!endReached && _uiState.value.reviewPhotos.size < minInitialHits) {
                loadChunk(reset = first)
                first = false
            }
        }
    }

    private suspend fun loadIgnoredPhotos() {
        _uiState.update { it.copy(isLoadingIgnored = true) }
        try {
            ignoredIds = ignoredPhotoService.getIgnoredAssetIds()
            val ignoredAssets = ignoredPhotoService.loadIgnoredAssets()
            ignoredIds = ignoredAssets.mapTo(linkedSetOf()) { it.id }
            knownPhotoIds += ignoredIds
            _uiState.update { state ->
                state.copy(ignoredPhotos = ignoredAssets.sortedByDescending { it.takenAt.toEpochMilliseconds() })
            }
        } finally {
            _uiState.update { it.copy(isLoadingIgnored = false) }
        }
    }

    private suspend fun loadChunk(reset: Boolean) {
        _uiState.update { state ->
            state.copy(
                isLoading = if (reset) true else state.isLoading,
                isLoadingMore = if (reset) state.isLoadingMore else true,
                progress = LoadProgress(
                    active = true,
                    scanned = scannedTotal,
                    total = scannedTotal,
                    found = foundTotal
                )
            )
        }

        try {
            val delta = service.loadNextChunkWithoutLocation(
                offset = candidateOffset,
                candidateLimit = candidatePageSize,
                batchSize = 24
            ) { scannedInChunk, chunkTotal, foundInChunk ->
                _uiState.update { state ->
                    state.copy(
                        progress = LoadProgress(
                            active = true,
                            scanned = scannedTotal + scannedInChunk,
                            total = scannedTotal + chunkTotal,
                            found = foundTotal + foundInChunk
                        )
                    )
                }
            }

            candidateOffset += delta.scannedInChunk
            scannedTotal += delta.scannedInChunk
            foundTotal += delta.foundInChunk
            endReached = delta.endReached

            if (delta.newHits.isNotEmpty()) {
                val uniqueHits = delta.newHits.filter { knownPhotoIds.add(it.id) }
                if (uniqueHits.isNotEmpty()) {
                    val ignoredHits = uniqueHits.filter { it.id in ignoredIds }
                    val reviewHits = uniqueHits.filterNot { it.id in ignoredIds }

                    _uiState.update { state ->
                        state.copy(
                            reviewPhotos = state.reviewPhotos + reviewHits,
                            ignoredPhotos = (state.ignoredPhotos + ignoredHits)
                                .sortedByDescending { it.takenAt.toEpochMilliseconds() }
                        )
                    }
                }
            }
        } finally {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    progress = state.progress.copy(active = false)
                )
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.selectedTab != PhotoListTab.Review) return
        if (_uiState.value.isLoadingMore || _uiState.value.isLoading || endReached) return
        scope.launch { loadChunk(reset = false) }
    }

    fun selectTab(tab: PhotoListTab) {
        if (tab == PhotoListTab.Ignored && _uiState.value.ignoredPhotos.isEmpty()) return
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun delete(asset: Asset) {
        scope.launch {
            val ok = service.deleteAsset(asset)
            if (ok) {
                removeFromList(asset.id, clearIgnored = true)
            } else {
                _events.tryEmit(PhotoListEvent.DeleteFailed)
            }
        }
    }

    fun ignore(asset: Asset) {
        scope.launch {
            ignoredPhotoService.ignore(asset.id)
            ignoredIds = ignoredIds + asset.id
            _uiState.update { state ->
                val remaining = state.reviewPhotos.filterNot { it.id == asset.id }
                val ignored = (state.ignoredPhotos + asset)
                    .distinctBy { it.id }
                    .sortedByDescending { it.takenAt.toEpochMilliseconds() }
                state.copy(reviewPhotos = remaining, ignoredPhotos = ignored)
            }

            while (!endReached && _uiState.value.reviewPhotos.size < minInitialHits) {
                loadChunk(reset = false)
            }
        }
    }

    fun restore(assetId: String) {
        scope.launch {
            ignoredPhotoService.restore(assetId)
            ignoredIds = ignoredIds - assetId
            _uiState.update { state ->
                val restored = state.ignoredPhotos.firstOrNull { it.id == assetId }
                val review = if (restored != null) {
                    (state.reviewPhotos + restored)
                        .distinctBy { it.id }
                        .sortedByDescending { it.takenAt.toEpochMilliseconds() }
                } else {
                    state.reviewPhotos
                }
                val ignored = state.ignoredPhotos.filterNot { it.id == assetId }
                state.copy(
                    reviewPhotos = review,
                    ignoredPhotos = ignored,
                    selectedTab = if (ignored.isEmpty()) PhotoListTab.Review else state.selectedTab
                )
            }
        }
    }

    private fun removeFromList(assetId: String, clearIgnored: Boolean) {
        scope.launch {
            if (clearIgnored && assetId in ignoredIds) {
                ignoredPhotoService.clear(assetId)
                ignoredIds = ignoredIds - assetId
            }
            knownPhotoIds.remove(assetId)
            _uiState.update { state ->
                val ignored = state.ignoredPhotos.filterNot { it.id == assetId }
                state.copy(
                    reviewPhotos = state.reviewPhotos.filterNot { it.id == assetId },
                    ignoredPhotos = ignored,
                    selectedTab = if (ignored.isEmpty()) PhotoListTab.Review else state.selectedTab
                )
            }
        }
    }
}
