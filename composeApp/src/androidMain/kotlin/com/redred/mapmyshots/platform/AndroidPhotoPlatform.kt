package com.redred.mapmyshots.platform

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.redred.mapmyshots.model.Asset
import kotlin.math.max
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val TAG = "PhotoRepo"

class AndroidPhotoRepository(private val context: Context): PhotoRepository {

    @OptIn(ExperimentalTime::class)
    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> {
        val baseUri: Uri = if (Build.VERSION.SDK_INT >= 29)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.MediaColumns.SIZE
        )
        val selection = "${MediaStore.MediaColumns.SIZE} > 0"
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(baseUri, projection, selection, null, sort)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val takenCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            val out = mutableListOf<Asset>()
            var row = 0
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val name = c.getString(nameCol)
                val takenMs = c.getLong(takenCol)
                val addedSec = c.getLong(addedCol)
                val ts = if (takenMs > 0) takenMs else max(addedSec, 0L) * 1000L

                val uri = ContentUris.withAppendedId(baseUri, id)
                out += Asset(
                    id = id.toString(),
                    displayName = name,
                    takenAt = Instant.fromEpochMilliseconds(if (ts > 0) ts else System.currentTimeMillis()),
                    uri = uri.toString()
                )
                row++
            }
            return out
        }
        return emptyList()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} BETWEEN ? AND ?"
        val args = arrayOf(min.toEpochMilliseconds().toString(), max.toEpochMilliseconds().toString())
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(uri, projection, selection, args, sort)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val out = mutableListOf<Asset>()
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val name = c.getString(nameCol)
                val takenMs = c.getLong(dateCol)
                val contentUri = ContentUris.withAppendedId(uri, id)
                out += Asset(
                    id = id.toString(),
                    displayName = name,
                    takenAt = Instant.fromEpochMilliseconds(takenMs),
                    uri = contentUri.toString()
                )
            }
            return out
        }
        return emptyList()
    }
}

class AndroidExifPlatform(private val context: Context): ExifPlatform {
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        val uri = asset.uri.toUri()
        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val exif = ExifInterface(pfd.fileDescriptor)
            val out = FloatArray(2)
            return if (exif.getLatLong(out)) out[0].toDouble() to out[1].toDouble() else null
        }
        return null
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean {
        val uri = asset.uri.toUri()
        context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
            val exif = ExifInterface(pfd.fileDescriptor)
            exif.setLatLong(lat, lon)
            exif.saveAttributes()
            return true
        }
        return false
    }
}

class AndroidGeocoderPlatform(private val context: Context): GeocoderPlatform {
    override suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val g = android.location.Geocoder(context)
            val list = g.getFromLocation(lat, lon, 1)
            if (!list.isNullOrEmpty()) {
                val p = list[0]
                listOfNotNull(p.locality, p.countryName).joinToString(", ")
            } else null
        } catch (_: Exception) { null }
    }
}