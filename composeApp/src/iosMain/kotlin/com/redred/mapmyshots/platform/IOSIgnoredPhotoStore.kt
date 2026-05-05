package com.redred.mapmyshots.platform

import platform.Foundation.NSArray
import platform.Foundation.NSUserDefaults

private const val KEY_IGNORED_IDS = "ignored_ids"
private const val SUITE_NAME = "mapmyshots_ignored_assets"

class IOSIgnoredPhotoStore : IgnoredPhotoStore {
    private val defaults = NSUserDefaults(suiteName = SUITE_NAME)

    override suspend fun getIgnoredAssetIds(): Set<String> {
        val values = defaults.stringArrayForKey(KEY_IGNORED_IDS) ?: emptyList<Any?>()
        return values.mapNotNull { it as? String }.toSet()
    }

    override suspend fun setIgnoredAssetIds(ids: Set<String>) {
        defaults.setObject(ids.toList() as NSArray, forKey = KEY_IGNORED_IDS)
    }
}
