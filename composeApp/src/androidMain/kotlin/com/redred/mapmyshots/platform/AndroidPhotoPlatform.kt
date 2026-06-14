package com.redred.mapmyshots.platform

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.redred.mapmyshots.AndroidDeleteCoordinator
import com.redred.mapmyshots.model.Asset
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidPhotoRepository(private val context: Context): PhotoRepository {
    @OptIn(ExperimentalTime::class)
    override suspend fun listImagesPage(
        offset: Int,
        limit: Int
    ): AssetPage {
        if (limit <= 0) return AssetPage(items = emptyList(), endReached = true)

        val baseUri = imagesUri()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.MediaColumns.SIZE
        )
        val selection = "${MediaStore.MediaColumns.SIZE} > 0"
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        val items = mutableListOf<Asset>()
        context.contentResolver.queryImages(
            uri = baseUri,
            projection = projection,
            selection = selection,
            selectionArgs = null,
            sortOrder = sort,
            offset = offset,
            limit = limit
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                items += cursor.toAsset(
                    idCol = idCol,
                    nameCol = nameCol,
                    takenCol = takenCol,
                    addedCol = addedCol,
                    baseUri = baseUri
                )
            }
        }

        return AssetPage(
            items = items,
            endReached = items.size < limit
        )
    }

    override suspend fun listImagesByIds(ids: List<String>): List<Asset> {
        if (ids.isEmpty()) return emptyList()

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
        val idOrder = ids.withIndex().associate { it.value to it.index }
        val placeholders = List(ids.size) { "?" }.joinToString(",")
        val selection = "${MediaStore.Images.Media._ID} IN ($placeholders) AND ${MediaStore.MediaColumns.SIZE} > 0"
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(baseUri, projection, selection, ids.toTypedArray(), sort)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val takenCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            val out = mutableListOf<Asset>()
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val name = c.getString(nameCol)
                val takenMs = c.getLong(takenCol)
                val addedSec = c.getLong(addedCol)
                val ts = if (takenMs > 0) takenMs else maxOf(addedSec, 0L) * 1000L
                val uri = ContentUris.withAppendedId(baseUri, id)
                out += Asset(
                    id = id.toString(),
                    displayName = name,
                    takenAt = Instant.fromEpochMilliseconds(if (ts > 0) ts else System.currentTimeMillis()),
                    uri = uri.toString()
                )
            }
            return out.sortedBy { idOrder[it.id] ?: Int.MAX_VALUE }
        }

        return emptyList()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> {
        val baseUri = imagesUri()
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

            while (c.moveToNext() && (limitPerAlbum <= 0 || out.size < limitPerAlbum)) {
                out += c.toAsset(
                    idCol = idCol,
                    nameCol = nameCol,
                    takenCol = takenCol,
                    addedCol = addedCol,
                    baseUri = baseUri
                )
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
        val uri = imagesUri()

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

    override suspend fun deleteAsset(asset: Asset): Boolean {
        val uri = asset.uri.toUri()
        return tryDelete(uri)
    }

    private suspend fun tryDelete(uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (error: RecoverableSecurityException) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            val granted = AndroidDeleteCoordinator.requestDeletePermission(
                error.userAction.actionIntent.intentSender
            )
            if (!granted) return false
            try {
                context.contentResolver.delete(uri, null, null) > 0
            } catch (_: Throwable) {
                false
            }
        } catch (_: SecurityException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}

class AndroidExifPlatform(private val context: Context): ExifPlatform {
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        val uri = requireOriginalUri(asset.uri.toUri())
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

private fun requireOriginalUri(uri: Uri): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.setRequireOriginal(uri)
    } else {
        uri
    }
}

private fun imagesUri(): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}

private fun ContentResolver.queryImages(
    uri: Uri,
    projection: Array<String>,
    selection: String?,
    selectionArgs: Array<String>?,
    sortOrder: String,
    offset: Int,
    limit: Int
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    query(
        uri,
        projection,
        Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        },
        null
    )
} else {
    val limitedSort = buildString {
        append(sortOrder)
        append(" LIMIT ")
        append(limit)
        append(" OFFSET ")
        append(offset)
    }
    query(uri, projection, selection, selectionArgs, limitedSort)
}

@OptIn(ExperimentalTime::class)
private fun android.database.Cursor.toAsset(
    idCol: Int,
    nameCol: Int,
    takenCol: Int,
    addedCol: Int,
    baseUri: Uri
): Asset {
    val id = getLong(idCol)
    val takenMs = getLong(takenCol)
    val addedSec = getLong(addedCol)
    val timestamp = when {
        takenMs > 0L -> takenMs
        addedSec > 0L -> addedSec * 1000L
        else -> System.currentTimeMillis()
    }
    return Asset(
        id = id.toString(),
        displayName = getString(nameCol),
        takenAt = Instant.fromEpochMilliseconds(timestamp),
        uri = ContentUris.withAppendedId(baseUri, id).toString()
    )
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
