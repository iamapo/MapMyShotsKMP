package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.service.PhotoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoadProgress(
    val scanned: Int = 0,
    val total: Int = 0,
    val found: Int = 0,
    val active: Boolean = false
) {
    val fraction: Float
        get() = if (!active || total <= 0) 0f
        else (scanned.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

class PhotoListViewModel(private val service: PhotoService) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _progress = MutableStateFlow(LoadProgress())
    val progress: StateFlow<LoadProgress> = _progress

    private val _photos = MutableStateFlow<List<Asset>>(emptyList())
    val photos: StateFlow<List<Asset>> = _photos

    private val candidatePageSize = 200
    private var candidateOffset = 0
    private var endReached = false

    private var scannedTotal = 0
    private var foundTotal = 0

    fun clear() = job.cancel()

    fun loadFirstPage() {
        if (_photos.value.isNotEmpty()) return
        candidateOffset = 0
        endReached = false
        scannedTotal = 0
        foundTotal = 0
        _photos.value = emptyList()
        loadInternal(reset = true)
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || _isLoading.value || endReached) return
        loadInternal(reset = false)
    }

    private fun loadInternal(reset: Boolean) {
        scope.launch {
            if (reset) _isLoading.value = true else _isLoadingMore.value = true
            _progress.value = LoadProgress(active = true, scanned = scannedTotal, total = scannedTotal, found = foundTotal)

            try {
                val delta = service.loadNextChunkWithoutLocation(
                    offset = candidateOffset,
                    candidateLimit = candidatePageSize,
                    batchSize = 24
                ) { scannedInChunk, chunkTotal, foundInChunk ->
                    _progress.value = LoadProgress(
                        active = true,
                        scanned = scannedTotal + scannedInChunk,
                        total = scannedTotal + chunkTotal,
                        found = foundTotal + foundInChunk
                    )
                }

                candidateOffset += delta.scannedInChunk
                scannedTotal += delta.scannedInChunk
                foundTotal += delta.foundInChunk
                endReached = delta.endReached

                if (delta.newHits.isNotEmpty()) {
                    _photos.value += delta.newHits
                }
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
                _progress.value = _progress.value.copy(active = false)
            }
        }
    }
}