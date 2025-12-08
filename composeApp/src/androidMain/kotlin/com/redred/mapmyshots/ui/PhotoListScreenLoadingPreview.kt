package com.redred.mapmyshots.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Preview(showBackground = true, name = "Photo List – Loading")
@Composable
fun PhotoListScreenLoadingPreview() {
    PhotoListScreenContent(
        isLoading = true,
        grouped = emptyMap(),
        onOpen = {}
    )
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true, name = "Photo List – With Data")
@Composable
fun PhotoListScreenWithDataPreview() {
    val assets = (1..10).map {
        Asset(
            id = it.toString(),
            uri = "https://placekitten.com/400/40$it",
            displayName = "Kitten $it",
            takenAt = Instant.fromEpochMilliseconds(0L)
        )
    }
    val grouped = mapOf("Januar 2025" to assets)

    PhotoListScreenContent(
        isLoading = false,
        grouped = grouped,
        onOpen = {}
    )
}
