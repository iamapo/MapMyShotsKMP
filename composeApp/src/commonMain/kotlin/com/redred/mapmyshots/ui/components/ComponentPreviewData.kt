package com.redred.mapmyshots.ui.components

import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal fun previewAsset(
    id: String = "preview_asset",
    displayName: String = "IMG_PREVIEW"
): Asset {
    return Asset(
        id = id,
        displayName = displayName,
        takenAt = Instant.fromEpochMilliseconds(1761472800000),
        uri = "content://preview/$id"
    )
}

@OptIn(ExperimentalTime::class)
internal fun previewSuggestionAsset(): Asset {
    return Asset(
        id = "preview_suggestion",
        displayName = "IMG_LOCATION",
        takenAt = Instant.fromEpochMilliseconds(1761469200000),
        uri = "content://preview/suggestion",
        hasLocation = true
    )
}
