package com.redred.mapmyshots.platform

interface IgnoredPhotoStore {
    suspend fun getIgnoredAssetIds(): Set<String>
    suspend fun setIgnoredAssetIds(ids: Set<String>)
}
