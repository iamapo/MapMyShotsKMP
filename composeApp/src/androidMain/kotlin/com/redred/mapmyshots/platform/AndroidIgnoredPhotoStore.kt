package com.redred.mapmyshots.platform

import android.content.Context

private const val PREFS_NAME = "mapmyshots_ignored_assets"
private const val KEY_IGNORED_IDS = "ignored_ids"

class AndroidIgnoredPhotoStore(context: Context) : IgnoredPhotoStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun getIgnoredAssetIds(): Set<String> {
        return prefs.getStringSet(KEY_IGNORED_IDS, emptySet()).orEmpty()
    }

    override suspend fun setIgnoredAssetIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_IGNORED_IDS, ids).apply()
    }
}
