package com.redred.mapmyshots.service

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
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

    @Test
    fun unreadableFileReturnsNullAndIsRetried() = runBlocking {
        val platform = RecordingExifPlatform(readResult = null, writeResult = false, failRead = true)
        val service = ExifService(platform)

        assertNull(service.getLatLon(asset("broken")))
        assertNull(service.getLatLon(asset("broken")))
        assertEquals(2, platform.readCount)
    }

    @Test
    fun nearbyLocationsAreGeocodedOnce() = runBlocking {
        val geocoder = CountingGeocoder()
        val service = ExifService(RecordingExifPlatform(readResult = null, writeResult = true))

        assertEquals("Place", service.getLocationName(48.10001, 11.60001, geocoder))
        assertEquals("Place", service.getLocationName(48.10002, 11.60002, geocoder))
        assertEquals(1, geocoder.calls)
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
    private val writeResult: Boolean,
    private val failRead: Boolean = false
) : ExifPlatform {
    var readCount: Int = 0

    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        readCount += 1
        if (failRead) throw IllegalStateException("unreadable")
        return readResult
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean = writeResult
}

private class CountingGeocoder : GeocoderPlatform {
    var calls: Int = 0

    override suspend fun reverseGeocode(lat: Double, lon: Double): String {
        calls += 1
        return "Place"
    }
}
