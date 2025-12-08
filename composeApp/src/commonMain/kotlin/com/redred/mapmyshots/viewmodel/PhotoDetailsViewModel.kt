package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.SimilarityService
import com.redred.mapmyshots.util.getTimeRangeDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhotoDetailsViewModel(
    val photo: Asset,
    private val exif: ExifService,
    private val sim: SimilarityService,
    private val geocoder: GeocoderPlatform,
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _timeRange = MutableStateFlow("1 hour")
    val timeRange: StateFlow<String> = _timeRange

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _similar = MutableStateFlow<List<Asset>>(emptyList())
    val similar: StateFlow<List<Asset>> = _similar

    private val _locationNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val locationNames: StateFlow<Map<String, String>> = _locationNames

    fun clear() {
        job.cancel()
    }

    fun setTimeRange(label: String) {
        _timeRange.value = label
        loadSimilar()
    }

    fun loadSimilar() {
        scope.launch {
            _loading.value = true
            try {
                val list = sim.findByTimeAndGps(photo, getTimeRangeDuration(_timeRange.value))
                _similar.value = list

                val names = mutableMapOf<String, String>()
                for (a in list) {
                    names[a.id] = exif.getLocationName(a, geocoder)
                }
                _locationNames.value = names
            } finally {
                _loading.value = false
            }
        }
    }

    suspend fun applyLocationFrom(src: Asset): Boolean {
        val pair = exif.getLatLon(src) ?: return false
        return exif.writeLatLon(photo, pair.first, pair.second)
    }
}