package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
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

data class PhotoListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val progress: LoadProgress = LoadProgress(),
    val photos: List<Asset> = emptyList()
)

sealed interface PhotoListIntent {
    data object LoadFirstPage : PhotoListIntent
    data object LoadNextPage : PhotoListIntent
    data class Delete(val asset: Asset) : PhotoListIntent
    data class RemoveFromList(val assetId: String) : PhotoListIntent
}

sealed interface PhotoListEvent {
    data object DeleteFailed : PhotoListEvent
}

class PhotoListViewModel(private val service: PhotoService) {
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
    fun clear() = job.cancel()

    fun onIntent(intent: PhotoListIntent) {
        when (intent) {
            PhotoListIntent.LoadFirstPage -> loadFirstPage()
            PhotoListIntent.LoadNextPage -> loadNextPage()
            is PhotoListIntent.Delete -> delete(intent.asset)
            is PhotoListIntent.RemoveFromList -> removeFromList(intent.assetId)
        }
    }

    fun loadFirstPage() {
        if (_uiState.value.photos.isNotEmpty()) return
        candidateOffset = 0
        endReached = false
        scannedTotal = 0
        foundTotal = 0
        _uiState.update { it.copy(photos = emptyList()) }
        scope.launch {
            var first = true
            while (!endReached && _uiState.value.photos.size < minInitialHits) {
                loadChunk(reset = first)
                first = false
            }
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
                _uiState.update { state ->
                    state.copy(photos = state.photos + delta.newHits)
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
        if (_uiState.value.isLoadingMore || _uiState.value.isLoading || endReached) return
        scope.launch { loadChunk(reset = false) }
    }

    fun delete(asset: Asset) {
        scope.launch {
            val ok = service.deleteAsset(asset)
            if (ok) {
                removeFromList(asset.id)
            } else {
                _events.tryEmit(PhotoListEvent.DeleteFailed)
            }
        }
    }

    private fun removeFromList(assetId: String) {
        _uiState.update { state ->
            state.copy(photos = state.photos.filterNot { it.id == assetId })
        }
    }
}
