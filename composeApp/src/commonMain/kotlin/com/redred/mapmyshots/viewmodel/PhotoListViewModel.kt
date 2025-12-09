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

class PhotoListViewModel(private val service: PhotoService) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _photos = MutableStateFlow<List<Asset>>(emptyList())
    val photos: StateFlow<List<Asset>> = _photos

    private val pageSize = 50
    private var pagingOffset = 0   // wie viele "globale" Assets schon betrachtet
    private var isLoadingMore = false
    private var endReached = false

    fun clear() {
        job.cancel()
    }

    fun loadFirstPage() {
        if (_photos.value.isNotEmpty()) return
        loadInternal(reset = true)
    }

    fun loadNextPage() {
        if (isLoadingMore || endReached) return
        loadInternal(reset = false)
    }

    private fun loadInternal(reset: Boolean) {
        scope.launch {
            isLoadingMore = true
            if (reset) {
                _isLoading.value = true
                pagingOffset = 0
                endReached = false
            }

            val newItems = service.loadPhotosPage(
                pagingOffset = pagingOffset,
                pageSize = pageSize
            )

            // Wir haben auf jeden Fall pageSize "globale" Assets gescannt,
            // auch wenn ein Teil davon Location hatte und rausgeflogen ist.
            pagingOffset += pageSize

            if (newItems.isEmpty()) {
                endReached = true
            }

            _photos.value = if (reset) {
                newItems
            } else {
                _photos.value + newItems
            }

            _isLoading.value = false
            isLoadingMore = false
        }
    }

    fun groupedByMonth() = groupByMonth(_photos.value)
}