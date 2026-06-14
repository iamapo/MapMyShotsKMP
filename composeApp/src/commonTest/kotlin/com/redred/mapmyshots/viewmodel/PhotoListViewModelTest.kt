package com.redred.mapmyshots.viewmodel

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.AssetPage
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.IgnoredPhotoStore
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.IgnoredPhotoService
import com.redred.mapmyshots.service.PhotoService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PhotoListViewModelTest {
    @Test
    fun loadFirstPageAppendsResultsAndStopsLoading() = runBlocking {
        val pageAssets = listOf(asset("1"), asset("2"))
        val repo = FakePagedPhotoRepository(
            pages = listOf(
                AssetPage(
                    items = pageAssets,
                    endReached = true
                )
            )
        )
        val service = PhotoService(
            repo = repo,
            exif = ExifService(FakeLatLonExifPlatform(emptySet()))
        )
        val vm = PhotoListViewModel(service, IgnoredPhotoService(FakeIgnoredPhotoStore(), repo))

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { !vm.uiState.value.isLoading && vm.uiState.value.reviewPhotos.size == 2 }

        assertEquals(pageAssets, vm.uiState.value.reviewPhotos)
        assertFalse(vm.uiState.value.progress.active)

        vm.clear()
    }

    @Test
    fun loadFailureEmitsEventAndStopsLoading() = runBlocking {
        val repo = FailingPagedPhotoRepository()
        val vm = PhotoListViewModel(
            PhotoService(
                repo = repo,
                exif = ExifService(FakeLatLonExifPlatform(emptySet()))
            ),
            IgnoredPhotoService(FakeIgnoredPhotoStore(), repo)
        )
        val event = async { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoListIntent.LoadFirstPage)

        assertEquals(PhotoListEvent.LoadFailed, event.await())
        waitUntil { !vm.uiState.value.isLoading && !vm.uiState.value.isLoadingMore }
        assertEquals(emptyList(), vm.uiState.value.reviewPhotos)

        vm.clear()
    }

    private suspend fun waitUntil(predicate: () -> Boolean) {
        withTimeout(1_000) {
            while (!predicate()) {
                kotlinx.coroutines.delay(10)
            }
        }
    }

    private fun asset(id: String) = Asset(
        id = id,
        displayName = "$id.jpg",
        takenAt = Instant.fromEpochMilliseconds(id.toLong() * 1_000),
        uri = "content://test/$id"
    )
}

private class FakePagedPhotoRepository(
    private val pages: List<AssetPage>
) : PhotoRepository {
    private var index = 0

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        return pages[index++]
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> = emptyList()

    override suspend fun listImagesBetween(
        min: Instant,
        max: Instant
    ): List<Asset> = emptyList()

    override suspend fun deleteAsset(asset: Asset): Boolean = true
}

private class FailingPagedPhotoRepository : PhotoRepository {
    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        throw IllegalStateException("boom")
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> = emptyList()

    override suspend fun listImagesBetween(
        min: Instant,
        max: Instant
    ): List<Asset> = emptyList()

    override suspend fun deleteAsset(asset: Asset): Boolean = true
}

private class FakeLatLonExifPlatform(
    private val assetsWithLocation: Set<String>
) : ExifPlatform {
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        return if (asset.id in assetsWithLocation) 48.1 to 11.6 else null
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean = true
}

private class FakeIgnoredPhotoStore : IgnoredPhotoStore {
    private var ids: Set<String> = emptySet()

    override suspend fun getIgnoredAssetIds(): Set<String> = ids

    override suspend fun setIgnoredAssetIds(ids: Set<String>) {
        this.ids = ids
    }
}
