package com.redred.mapmyshots.viewmodel

import androidx.lifecycle.viewModelScope
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.platform.AssetPage
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.service.SimilarityService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PhotoDetailsViewModelTest {
    @Test
    fun changingTimeWindowReloadsSimilarPhotosWithNewRange() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val candidate = asset("candidate", 101.hours.inWholeMilliseconds, hasLocation = true)
        val repo = RecordingPhotoRepository(listOf(candidate))
        val exif = FakeExifPlatform(latLon = mapOf(candidate.id to (48.1 to 11.6)))
        val vm = viewModel(target, repo, exif)

        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
        waitUntil { repo.windows.size == 1 && !vm.uiState.value.loading }

        vm.onIntent(PhotoDetailsIntent.SetTimeWindow(TimeWindow.FourHours))
        waitUntil { repo.windows.size == 2 && !vm.uiState.value.loading }

        assertEquals(TimeWindow.FourHours, vm.uiState.value.timeWindow)
        assertEquals(1, vm.uiState.value.similar.size)
        assertEquals(1.hours.inWholeMilliseconds, repo.windows[0].radiusAround(target))
        assertEquals(4.hours.inWholeMilliseconds, repo.windows[1].radiusAround(target))

        vm.viewModelScope.cancel()
    }

    @Test
    fun applyLocationEmitsMissingSourceLocationWhenSourceHasNoGps() = runBlocking {
        val target = asset("target", 1000)
        val source = asset("source", 2000)
        val vm = viewModel(
            target = target,
            repo = RecordingPhotoRepository(emptyList()),
            exifPlatform = FakeExifPlatform(latLon = emptyMap())
        )

        val event = async { withTimeout(1_000) { vm.events.first() } }
        yield()

        vm.onIntent(PhotoDetailsIntent.ApplyLocationFrom(source))

        assertEquals(PhotoDetailsEvent.Error(PhotoDetailsError.MissingSourceLocation), event.await())

        vm.viewModelScope.cancel()
    }

    @Test
    fun applyLocationEmitsWriteFailedWhenExifWriteFails() = runBlocking {
        val target = asset("target", 1000)
        val source = asset("source", 2000)
        val vm = viewModel(
            target = target,
            repo = RecordingPhotoRepository(emptyList()),
            exifPlatform = FakeExifPlatform(
                latLon = mapOf(source.id to (48.1 to 11.6)),
                writeResult = false
            )
        )

        val event = async { withTimeout(1_000) { vm.events.first() } }
        yield()

        vm.onIntent(PhotoDetailsIntent.ApplyLocationFrom(source))

        assertEquals(PhotoDetailsEvent.Error(PhotoDetailsError.WriteFailed), event.await())

        vm.viewModelScope.cancel()
    }

    @Test
    fun applyManualLocationWritesCoordinatesAndEmitsSavedLocation() = runBlocking {
        val target = asset("target", 1000)
        val exif = FakeExifPlatform(latLon = emptyMap())
        val vm = viewModel(
            target = target,
            repo = RecordingPhotoRepository(emptyList()),
            exifPlatform = exif
        )

        val event = async { withTimeout(1_000) { vm.events.first() } }
        yield()

        vm.onIntent(PhotoDetailsIntent.ApplyManualLocation(52.52, 13.405))

        assertEquals(PhotoDetailsEvent.Saved("Test Location"), event.await())
        assertEquals(listOf(Write(target.id, 52.52, 13.405)), exif.writes)
        assertEquals(52.52 to 13.405, vm.uiState.value.currentLocation)

        vm.viewModelScope.cancel()
    }

    @Test
    fun latestSimilarLoadWinsWhenTimeWindowChangesQuickly() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val oneHourCandidate = asset("one-hour", 100.hours.inWholeMilliseconds + 1_000, hasLocation = true)
        val fourHourCandidate = asset("four-hour", 100.hours.inWholeMilliseconds + 2_000, hasLocation = true)
        val repo = DelayedSimilarityRepository(
            target = target,
            oneHourResult = listOf(oneHourCandidate),
            fourHourResult = listOf(fourHourCandidate)
        )
        val exif = FakeExifPlatform(
            latLon = mapOf(
                oneHourCandidate.id to (48.1 to 11.6),
                fourHourCandidate.id to (48.2 to 11.7)
            )
        )
        val vm = viewModel(target, repo, exif)

        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
        delay(20)
        vm.onIntent(PhotoDetailsIntent.SetTimeWindow(TimeWindow.FourHours))
        waitUntil { !vm.uiState.value.loading }

        assertEquals(TimeWindow.FourHours, vm.uiState.value.timeWindow)
        assertEquals(listOf(fourHourCandidate), vm.uiState.value.similar)

        vm.viewModelScope.cancel()
    }

    @Test
    fun loadSimilarResolvesLocationsAndNamesOnlyForPhotosWithGps() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val withGps = asset("with-gps", 100.hours.inWholeMilliseconds + 1_000)
        val withoutGps = asset("without-gps", 100.hours.inWholeMilliseconds + 2_000)
        val repo = RecordingPhotoRepository(listOf(target, withGps, withoutGps))
        val exif = FakeExifPlatform(latLon = mapOf(withGps.id to (48.1 to 11.6)))
        val vm = viewModel(target, repo, exif)

        vm.onIntent(PhotoDetailsIntent.LoadSimilar)
        waitUntil { repo.windows.size == 1 && !vm.uiState.value.loading }

        val state = vm.uiState.value
        assertEquals(listOf(withGps), state.similar)
        assertEquals(mapOf(withGps.id to (48.1 to 11.6)), state.suggestionLocations)
        assertEquals(mapOf(withGps.id to "Test Location"), state.locationNames)
        assertEquals(null, state.currentLocation)

        vm.viewModelScope.cancel()
    }

    @Test
    fun applyLocationFromSourceUpdatesCurrentLocation() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val source = asset("source", 100.hours.inWholeMilliseconds + 1_000)
        val exif = FakeExifPlatform(latLon = mapOf(source.id to (48.1 to 11.6)))
        val vm = viewModel(target, RecordingPhotoRepository(emptyList()), exif)
        val event = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoDetailsIntent.ApplyLocationFrom(source))

        assertEquals(PhotoDetailsEvent.Saved("Test Location"), event.await())
        assertEquals(48.1 to 11.6, vm.uiState.value.currentLocation)
        assertEquals(listOf(Write(target.id, 48.1, 11.6)), exif.writes)

        vm.viewModelScope.cancel()
    }

    @Test
    fun deleteEmitsDeleted() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val vm = viewModel(target, RecordingPhotoRepository(emptyList()), FakeExifPlatform(emptyMap()))
        val event = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoDetailsIntent.Delete)

        assertEquals(PhotoDetailsEvent.Deleted, event.await())

        vm.viewModelScope.cancel()
    }

    @Test
    fun deleteFailureEmitsDeleteFailed() = runBlocking {
        val target = asset("target", 100.hours.inWholeMilliseconds)
        val repo = RecordingPhotoRepository(emptyList(), deleteResult = false)
        val vm = viewModel(target, repo, FakeExifPlatform(emptyMap()))
        val event = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoDetailsIntent.Delete)

        assertEquals(PhotoDetailsEvent.DeleteFailed, event.await())

        vm.viewModelScope.cancel()
    }

    private fun viewModel(
        target: Asset,
        repo: PhotoRepository,
        exifPlatform: FakeExifPlatform
    ): PhotoDetailsViewModel {
        val exif = ExifService(exifPlatform)
        return PhotoDetailsViewModel(
            photo = target,
            exif = exif,
            photoService = PhotoService(repo, exif),
            sim = SimilarityService(repo, exif),
            geocoder = FakeGeocoderPlatform()
        )
    }

    private suspend fun waitUntil(predicate: () -> Boolean) {
        withTimeout(1_000) {
            while (!predicate()) {
                delay(10)
            }
        }
    }

    private fun Window.radiusAround(asset: Asset): Long {
        return asset.takenAt.toEpochMilliseconds() - min.toEpochMilliseconds()
    }

    private fun asset(
        id: String,
        epochMillis: Long,
        hasLocation: Boolean? = null
    ): Asset {
        return Asset(
            id = id,
            displayName = "$id.jpg",
            takenAt = Instant.fromEpochMilliseconds(epochMillis),
            uri = "content://test/$id",
            hasLocation = hasLocation
        )
    }
}

private class DelayedSimilarityRepository(
    private val target: Asset,
    private val oneHourResult: List<Asset>,
    private val fourHourResult: List<Asset>
) : PhotoRepository {
    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        return AssetPage(emptyList(), endReached = true)
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        val radius = target.takenAt.toEpochMilliseconds() - min.toEpochMilliseconds()
        return if (radius == 1.hours.inWholeMilliseconds) {
            delay(200)
            oneHourResult
        } else {
            delay(10)
            fourHourResult
        }
    }

    override suspend fun deleteAsset(asset: Asset): Boolean = true
}

private data class Window(
    val min: Instant,
    val max: Instant
)

private data class Write(
    val assetId: String,
    val lat: Double,
    val lon: Double
)

private class RecordingPhotoRepository(
    private val imagesBetween: List<Asset>,
    private val deleteResult: Boolean = true
) : PhotoRepository {
    val windows = mutableListOf<Window>()

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        return AssetPage(emptyList(), endReached = true)
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        windows += Window(min, max)
        return imagesBetween
    }

    override suspend fun deleteAsset(asset: Asset): Boolean = deleteResult
}

private class FakeExifPlatform(
    private val latLon: Map<String, Pair<Double, Double>?>,
    private val writeResult: Boolean = true
) : ExifPlatform {
    val writes = mutableListOf<Write>()

    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? = latLon[asset.id]

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean {
        writes += Write(asset.id, lat, lon)
        return writeResult
    }
}

private class FakeGeocoderPlatform : GeocoderPlatform {
    override suspend fun reverseGeocode(lat: Double, lon: Double): String = "Test Location"
}
