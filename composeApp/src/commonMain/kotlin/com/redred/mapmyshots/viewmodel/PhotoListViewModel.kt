package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.util.groupByMonth
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

    private val pageSize = 50
    private var currentMax = pageSize
    private var endReached = false

    fun clear() {
        job.cancel()
    }

    fun loadFirstPage() {
        if (_photos.value.isNotEmpty()) return
        currentMax = pageSize
        endReached = false
        loadInternal(reset = true)
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || _isLoading.value || endReached) return
        currentMax += pageSize
        loadInternal(reset = false)
    }

    private fun loadInternal(reset: Boolean) {
        scope.launch {
            if (reset) _isLoading.value = true else _isLoadingMore.value = true
            _progress.value = LoadProgress(active = true, scanned = 0, total = 0, found = 0)

            try {
                val result = service.loadPhotosPage(
                    maxCount = currentMax,
                    batchSize = 24
                ) { scanned, total, found ->
                    _progress.value = LoadProgress(active = true, scanned = scanned, total = total, found = found)
                }

                // Ende nur, wenn repo weniger Kandidaten liefern kann als angefragt
                if (result.candidateCount < currentMax) {
                    endReached = true
                }

                _photos.value = result.items
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
                _progress.value = _progress.value.copy(active = false)
            }
        }
    }

    fun groupedByMonth() = groupByMonth(_photos.value)
}
