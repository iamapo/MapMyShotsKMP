package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.model.TimeWindow
import com.redred.mapmyshots.platform.AssetPage
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.service.SimilarityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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

        vm.clear()
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

        vm.clear()
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

        vm.clear()
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

        vm.clear()
    }

    private fun viewModel(
        target: Asset,
        repo: RecordingPhotoRepository,
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
    private val imagesBetween: List<Asset>
) : PhotoRepository {
    val windows = mutableListOf<Window>()

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        return AssetPage(emptyList(), endReached = true)
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> = emptyList()

    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        windows += Window(min, max)
        return imagesBetween
    }

    override suspend fun deleteAsset(asset: Asset): Boolean = true
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
