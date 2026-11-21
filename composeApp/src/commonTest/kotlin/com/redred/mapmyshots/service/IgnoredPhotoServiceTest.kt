package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.AssetPage
import com.redred.mapmyshots.platform.IgnoredPhotoStore
import com.redred.mapmyshots.platform.PhotoRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class IgnoredPhotoServiceTest {
    @Test
    fun ignoreAndRestoreUpdateStore() = runBlocking {
        val store = InMemoryIgnoredPhotoStore()
        val service = IgnoredPhotoService(store, ByIdPhotoRepository(emptyList()))

        service.ignore("a")
        service.ignore("b")
        service.restore("a")

        assertEquals(setOf("b"), service.getIgnoredAssetIds())
    }

    @Test
    fun loadIgnoredAssetsDropsMissingAndLocatedPhotosFromStore() = runBlocking {
        val store = InMemoryIgnoredPhotoStore(setOf("withoutGps", "withGps", "deleted"))
        val repo = ByIdPhotoRepository(
            listOf(
                asset("withoutGps"),
                asset("withGps", hasLocation = true)
            )
        )
        val service = IgnoredPhotoService(store, repo)

        val assets = service.loadIgnoredAssets()

        assertEquals(listOf("withoutGps"), assets.map { it.id })
        assertEquals(setOf("withoutGps"), store.getIgnoredAssetIds())
    }

    @Test
    fun loadIgnoredAssetsSkipsRepositoryWhenNothingIsIgnored() = runBlocking {
        val repo = ByIdPhotoRepository(listOf(asset("a")))
        val service = IgnoredPhotoService(InMemoryIgnoredPhotoStore(), repo)

        assertEquals(emptyList(), service.loadIgnoredAssets())
        assertEquals(0, repo.byIdRequests)
    }

    private fun asset(id: String, hasLocation: Boolean? = null) = Asset(
        id = id,
        displayName = "$id.jpg",
        takenAt = Instant.fromEpochMilliseconds(1_000),
        uri = "content://test/$id",
        hasLocation = hasLocation
    )
}

private class InMemoryIgnoredPhotoStore(
    private var ids: Set<String> = emptySet()
) : IgnoredPhotoStore {
    override suspend fun getIgnoredAssetIds(): Set<String> = ids

    override suspend fun setIgnoredAssetIds(ids: Set<String>) {
        this.ids = ids
    }
}

private class ByIdPhotoRepository(
    private val assets: List<Asset>
) : PhotoRepository {
    var byIdRequests: Int = 0
        private set

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage = AssetPage(emptyList(), endReached = true)

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> {
        byIdRequests += 1
        return assets.filter { it.id in ids }
    }

    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> = emptyList()

    override suspend fun deleteAsset(asset: Asset): Boolean = true
}
