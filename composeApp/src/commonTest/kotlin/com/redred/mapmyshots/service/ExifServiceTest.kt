package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.ExifPlatform
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ExifServiceTest {
    @Test
    fun writeLatLonUpdatesCachedCoordinates() = runBlocking {
        val asset = asset("photo")
        val platform = RecordingExifPlatform(readResult = null, writeResult = true)
        val service = ExifService(platform)

        assertNull(service.getLatLon(asset))

        val written = service.writeLatLon(asset, 48.1, 11.6)

        assertEquals(true, written)
        assertEquals(48.1 to 11.6, service.getLatLon(asset))
        assertEquals(1, platform.readCount)
    }

    private fun asset(id: String) = Asset(
        id = id,
        displayName = "$id.jpg",
        takenAt = Instant.fromEpochMilliseconds(1_000),
        uri = "content://test/$id"
    )
}

private class RecordingExifPlatform(
    private val readResult: Pair<Double, Double>?,
    private val writeResult: Boolean
) : ExifPlatform {
    var readCount: Int = 0

    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        readCount += 1
        return readResult
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean = writeResult
}
