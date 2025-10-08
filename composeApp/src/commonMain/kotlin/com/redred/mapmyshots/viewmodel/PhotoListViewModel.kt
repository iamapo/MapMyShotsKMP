package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.util.groupByMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class PhotoListViewModel(private val service: PhotoService) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _photos = MutableStateFlow<List<Asset>>(emptyList())
    val photos: StateFlow<List<Asset>> = _photos

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