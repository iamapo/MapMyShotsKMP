package com.redred.mapmyshots.viewmodel

import androidx.lifecycle.viewModelScope
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.AssetPage
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.IgnoredPhotoStore
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.IgnoredPhotoService
import com.redred.mapmyshots.service.PhotoService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
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

        vm.viewModelScope.cancel()
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
        val event = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoListIntent.LoadFirstPage)

        assertEquals(PhotoListEvent.LoadFailed, event.await())
        waitUntil { !vm.uiState.value.isLoading && !vm.uiState.value.isLoadingMore }
        assertEquals(emptyList(), vm.uiState.value.reviewPhotos)

        vm.viewModelScope.cancel()
    }

    @Test
    fun reloadRestartsScanFromFirstPage() = runBlocking {
        val repo = FakePagedPhotoRepository(
            pages = listOf(
                AssetPage(
                    items = listOf(asset("1")),
                    endReached = true
                ),
                AssetPage(
                    items = listOf(asset("2")),
                    endReached = true
                )
            )
        )
        val vm = PhotoListViewModel(
            PhotoService(
                repo = repo,
                exif = ExifService(FakeLatLonExifPlatform(emptySet()))
            ),
            IgnoredPhotoService(FakeIgnoredPhotoStore(), repo)
        )

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos == listOf(asset("1")) }
        vm.reload()
        waitUntil { vm.uiState.value.reviewPhotos == listOf(asset("2")) }

        // The list was reset, not appended to.
        assertEquals(listOf(asset("2")), vm.uiState.value.reviewPhotos)

        vm.viewModelScope.cancel()
    }

    @Test
    fun loadsFurtherChunksUntilMinimumHitsAreFound() = runBlocking {
        // First chunk (200) only has photos with GPS, so the scan must continue into the second chunk.
        val gallery = GalleryPhotoRepository(assets(0 until 250))
        val vm = listViewModel(gallery, withLocation = (0 until 200).map { "$it" }.toSet())

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 50 && !vm.uiState.value.isLoadingMore }

        assertEquals(2, gallery.pageRequests)
        assertEquals("200", vm.uiState.value.reviewPhotos.first().id)
        assertEquals(250, vm.uiState.value.progress.scanned)

        vm.viewModelScope.cancel()
    }

    @Test
    fun loadNextPageAppendsNextChunk() = runBlocking {
        val gallery = GalleryPhotoRepository(assets(0 until 300))
        val vm = listViewModel(gallery)

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 200 && !vm.uiState.value.isLoading }
        // LoadNextPage is dropped while the mutex is still held, so re-send it until it is accepted.
        waitUntil {
            vm.onIntent(PhotoListIntent.LoadNextPage)
            vm.uiState.value.reviewPhotos.size == 300
        }

        assertEquals(2, gallery.pageRequests)
        assertEquals(300, vm.uiState.value.reviewPhotos.map { it.id }.toSet().size)

        vm.viewModelScope.cancel()
    }

    @Test
    fun deletingScannedPhotoDoesNotSkipNextPhoto() = runBlocking {
        val gallery = GalleryPhotoRepository(assets(0 until 201))
        val vm = listViewModel(gallery)

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 200 && !vm.uiState.value.isLoading }
        vm.onIntent(PhotoListIntent.Delete(asset("5")))
        waitUntil { vm.uiState.value.reviewPhotos.none { it.id == "5" } }
        waitUntil {
            vm.onIntent(PhotoListIntent.LoadNextPage)
            vm.uiState.value.reviewPhotos.any { it.id == "200" }
        }

        assertEquals(200, vm.uiState.value.reviewPhotos.size)

        vm.viewModelScope.cancel()
    }

    @Test
    fun deleteFailureKeepsPhotoAndEmitsEvent() = runBlocking {
        val gallery = GalleryPhotoRepository(assets(0 until 3), deleteResult = false)
        val vm = listViewModel(gallery)
        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 3 && !vm.uiState.value.isLoading }
        val event = async(start = CoroutineStart.UNDISPATCHED) { withTimeout(1_000) { vm.events.first() } }

        vm.onIntent(PhotoListIntent.Delete(asset("1")))

        assertEquals(PhotoListEvent.DeleteFailed, event.await())
        assertEquals(3, vm.uiState.value.reviewPhotos.size)

        vm.viewModelScope.cancel()
    }

    @Test
    fun ignoreMovesPhotoToIgnoredListAndPersistsIt() = runBlocking {
        val store = FakeIgnoredPhotoStore()
        val gallery = GalleryPhotoRepository(assets(0 until 3))
        val vm = listViewModel(gallery, store = store)
        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 3 && !vm.uiState.value.isLoading }

        vm.onIntent(PhotoListIntent.Ignore(asset("1")))
        waitUntil { vm.uiState.value.ignoredPhotos.size == 1 }

        assertEquals(listOf("0", "2"), vm.uiState.value.reviewPhotos.map { it.id })
        assertEquals(setOf("1"), store.getIgnoredAssetIds())

        vm.onIntent(PhotoListIntent.SelectTab(PhotoListTab.Ignored))
        assertEquals(listOf(asset("1")), vm.uiState.value.visiblePhotos)

        vm.viewModelScope.cancel()
    }

    @Test
    fun restoreMovesPhotoBackAndReturnsToReviewTab() = runBlocking {
        val store = FakeIgnoredPhotoStore()
        val gallery = GalleryPhotoRepository(assets(0 until 3))
        val vm = listViewModel(gallery, store = store)
        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 3 && !vm.uiState.value.isLoading }
        vm.onIntent(PhotoListIntent.Ignore(asset("1")))
        waitUntil { vm.uiState.value.ignoredPhotos.size == 1 }
        vm.onIntent(PhotoListIntent.SelectTab(PhotoListTab.Ignored))

        vm.onIntent(PhotoListIntent.Restore("1"))
        waitUntil { vm.uiState.value.ignoredPhotos.isEmpty() }

        // Restored photos are re-sorted newest first.
        assertEquals(listOf("2", "1", "0"), vm.uiState.value.reviewPhotos.map { it.id })
        assertEquals(PhotoListTab.Review, vm.uiState.value.selectedTab)
        assertEquals(emptySet<String>(), store.getIgnoredAssetIds())

        vm.viewModelScope.cancel()
    }

    @Test
    fun previouslyIgnoredPhotosStayOutOfReviewList() = runBlocking {
        val store = FakeIgnoredPhotoStore()
        store.setIgnoredAssetIds(setOf("1"))
        val gallery = GalleryPhotoRepository(assets(0 until 3))
        val vm = listViewModel(gallery, store = store)

        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { !vm.uiState.value.isLoading && vm.uiState.value.reviewPhotos.size == 2 }

        assertEquals(listOf("1"), vm.uiState.value.ignoredPhotos.map { it.id })
        assertEquals(listOf("0", "2"), vm.uiState.value.reviewPhotos.map { it.id })

        vm.viewModelScope.cancel()
    }

    @Test
    fun ignoredTabCannotBeSelectedWhenEmpty() {
        val gallery = GalleryPhotoRepository(emptyList())
        val vm = listViewModel(gallery)

        vm.onIntent(PhotoListIntent.SelectTab(PhotoListTab.Ignored))

        assertEquals(PhotoListTab.Review, vm.uiState.value.selectedTab)

        vm.viewModelScope.cancel()
    }

    @Test
    fun removeFromListDropsPhotoWithoutDeleting() = runBlocking {
        val gallery = GalleryPhotoRepository(assets(0 until 3))
        val vm = listViewModel(gallery)
        vm.onIntent(PhotoListIntent.LoadFirstPage)
        waitUntil { vm.uiState.value.reviewPhotos.size == 3 && !vm.uiState.value.isLoading }

        vm.onIntent(PhotoListIntent.RemoveFromList("1"))
        waitUntil { vm.uiState.value.reviewPhotos.size == 2 }

        assertEquals(3, gallery.size)

        vm.viewModelScope.cancel()
    }

    private fun listViewModel(
        gallery: GalleryPhotoRepository,
        withLocation: Set<String> = emptySet(),
        store: FakeIgnoredPhotoStore = FakeIgnoredPhotoStore()
    ) = PhotoListViewModel(
        PhotoService(
            repo = gallery,
            exif = ExifService(FakeLatLonExifPlatform(withLocation))
        ),
        IgnoredPhotoService(store, gallery)
    )

    private fun assets(range: IntRange): List<Asset> = range.map { asset("$it") }

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

    override suspend fun listImagesBetween(
        min: Instant,
        max: Instant
    ): List<Asset> = emptyList()

    override suspend fun deleteAsset(asset: Asset): Boolean = true
}

/** Offset-based gallery like MediaStore: deleting an asset shifts all later offsets. */
private class GalleryPhotoRepository(
    assets: List<Asset>,
    private val deleteResult: Boolean = true
) : PhotoRepository {
    private val gallery = assets.toMutableList()
    var pageRequests: Int = 0
        private set
    val size: Int
        get() = gallery.size

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        pageRequests += 1
        val items = gallery.drop(offset).take(limit)
        return AssetPage(
            items = items,
            endReached = offset + limit >= gallery.size
        )
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = gallery.filter { it.id in ids }

    override suspend fun listImagesBetween(
        min: Instant,
        max: Instant
    ): List<Asset> = emptyList()

    override suspend fun deleteAsset(asset: Asset): Boolean {
        if (deleteResult) {
            gallery.removeAll { it.id == asset.id }
        }
        return deleteResult
    }
}

private class FailingPagedPhotoRepository : PhotoRepository {
    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        throw IllegalStateException("boom")
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> = emptyList()

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
