package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.IgnoredPhotoStore
import com.redred.mapmyshots.platform.PhotoRepository

class IgnoredPhotoService(
    private val store: IgnoredPhotoStore,
    private val repo: PhotoRepository
) {
    suspend fun getIgnoredAssetIds(): Set<String> = store.getIgnoredAssetIds()

    suspend fun ignore(assetId: String) {
        val updated = store.getIgnoredAssetIds().toMutableSet()
        updated += assetId
        store.setIgnoredAssetIds(updated)
    }

    suspend fun restore(assetId: String) {
        val updated = store.getIgnoredAssetIds().toMutableSet()
        updated -= assetId
        store.setIgnoredAssetIds(updated)
    }

    suspend fun clear(assetId: String) {
        restore(assetId)
    }

    suspend fun loadIgnoredAssets(): List<Asset> {
        val ids = store.getIgnoredAssetIds().toList()
        if (ids.isEmpty()) return emptyList()

        val assets = repo.listImagesByIds(ids).filter { it.hasLocation != true }
        val remainingIds = assets.mapTo(linkedSetOf()) { it.id }
        if (remainingIds.size != ids.size || remainingIds != ids.toSet()) {
            store.setIgnoredAssetIds(remainingIds)
        }
        return assets
    }
}
