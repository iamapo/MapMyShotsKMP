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
import kotlin.time.ExperimentalTime

class PhotoListViewModel(private val service: PhotoService) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _photos = MutableStateFlow<List<Asset>>(emptyList())
    val photos: StateFlow<List<Asset>> = _photos

    private val pageSize = 50
    private var currentMax = pageSize
    private var isLoadingMore = false
    private var endReached = false

    fun clear() {
        job.cancel()
    }

    @OptIn(ExperimentalTime::class)
    fun loadFirstPage() {
        if (_photos.value.isNotEmpty()) return
        loadInternal(reset = true)
    }

    fun loadNextPage() {
        if (isLoadingMore || endReached) return
        currentMax += pageSize
        loadInternal(reset = false)
    }

    private fun loadInternal(reset: Boolean) {
        scope.launch {
            isLoadingMore = true
            if (reset) _isLoading.value = true

            val list = service.loadPhotosPage(maxCount = currentMax)

            if (list.size <= _photos.value.size) {
                endReached = true
            }

            _photos.value = list
            _isLoading.value = false
            isLoadingMore = false
        }
    }

    @OptIn(ExperimentalTime::class)
    fun load() {
        scope.launch {
            try {
                _isLoading.value = true
                val list = service.loadPhotosWithoutLocation()
                _photos.value = list.sortedByDescending { it.takenAt }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun groupedByMonth() = groupByMonth(_photos.value)
}