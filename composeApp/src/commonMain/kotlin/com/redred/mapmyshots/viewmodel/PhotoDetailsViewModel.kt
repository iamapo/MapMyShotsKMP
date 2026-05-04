package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.SimilarityService
import com.redred.mapmyshots.util.getTimeRangeDuration
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

data class PhotoDetailsUiState(
    val timeWindow: TimeWindow = TimeWindow.OneHour,
    val loading: Boolean = false,
    val similar: List<Asset> = emptyList(),
    val locationNames: Map<String, String> = emptyMap()
)

sealed interface PhotoDetailsIntent {
    data object LoadSimilar : PhotoDetailsIntent
    data class SetTimeWindow(val timeWindow: TimeWindow) : PhotoDetailsIntent
    data class ApplyLocationFrom(val source: Asset) : PhotoDetailsIntent
}

sealed interface PhotoDetailsEvent {
    data object Saved : PhotoDetailsEvent
    data class Error(val reason: PhotoDetailsError) : PhotoDetailsEvent
}

enum class PhotoDetailsError {
    MissingSourceLocation,
    WriteFailed
}

class PhotoDetailsViewModel(
    val photo: Asset,
    private val exif: ExifService,
    private val sim: SimilarityService,
    private val geocoder: GeocoderPlatform,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _uiState = MutableStateFlow(PhotoDetailsUiState())
    val uiState: StateFlow<PhotoDetailsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PhotoDetailsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PhotoDetailsEvent> = _events.asSharedFlow()

    fun clear() {
        job.cancel()
    }

    fun onIntent(intent: PhotoDetailsIntent) {
        when (intent) {
            PhotoDetailsIntent.LoadSimilar -> loadSimilar()
            is PhotoDetailsIntent.SetTimeWindow -> setTimeWindow(intent.timeWindow)
            is PhotoDetailsIntent.ApplyLocationFrom -> applyLocationFrom(intent.source)
        }
    }

    fun setTimeWindow(timeWindow: TimeWindow) {
        _uiState.update { it.copy(timeWindow = timeWindow) }
        loadSimilar()
    }

    fun loadSimilar() {
        scope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                val currentRange = _uiState.value.timeWindow
                val list = sim.findByTimeAndGps(photo, getTimeRangeDuration(currentRange))

                val names = mutableMapOf<String, String>()
                for (a in list) {
                    names[a.id] = exif.getLocationName(a, geocoder)
                }
                _uiState.update {
                    it.copy(
                        similar = list,
                        locationNames = names
                    )
                }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun applyLocationFrom(src: Asset) {
        scope.launch {
            val pair = exif.getLatLon(src)
            if (pair == null) {
                _events.tryEmit(PhotoDetailsEvent.Error(PhotoDetailsError.MissingSourceLocation))
                return@launch
            }

            val ok = exif.writeLatLon(photo, pair.first, pair.second)
            if (ok) {
                _events.tryEmit(PhotoDetailsEvent.Saved)
            } else {
                _events.tryEmit(PhotoDetailsEvent.Error(PhotoDetailsError.WriteFailed))
            }
        }
    }
}
