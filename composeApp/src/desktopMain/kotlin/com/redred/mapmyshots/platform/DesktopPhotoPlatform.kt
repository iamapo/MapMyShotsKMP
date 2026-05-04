package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset
import java.net.URI
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DesktopPhotoRepository : PhotoRepository {
    private val imageRoot: Path
        get() {
            val configured = System.getenv("MAPMYSHOTS_PHOTOS_DIR")
            return if (!configured.isNullOrBlank()) {
                Paths.get(configured)
            } else {
                Paths.get(System.getProperty("user.home"), "Pictures")
            }
        }

    override suspend fun listImagesPage(offset: Int, limit: Int): AssetPage {
        val all = loadImages()
        return AssetPage(
            items = all.drop(offset).take(limit),
            endReached = offset + limit >= all.size
        )
    }

    override suspend fun listAllImages(limitPerAlbum: Int): List<Asset> = loadImages()

    override suspend fun listImagesBetween(min: Instant, max: Instant): List<Asset> {
        return loadImages().filter { it.takenAt >= min && it.takenAt <= max }
    }

    override suspend fun deleteAsset(asset: Asset): Boolean {
        return runCatching {
            Files.deleteIfExists(asset.path())
        }.getOrDefault(false)
    }

    @OptIn(ExperimentalTime::class)
    private fun loadImages(): List<Asset> {
        val root = imageRoot
        if (!Files.exists(root)) return emptyList()

        val assets = mutableListOf<Asset>()
        Files.walkFileTree(
            root,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    return if (dir != root && dir.shouldSkipDirectory()) {
                        FileVisitResult.SKIP_SUBTREE
                    } else {
                        FileVisitResult.CONTINUE
                    }
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (attrs.isRegularFile && file.isSupportedImage()) {
                        assets += Asset(
                            id = file.absolutePathString(),
                            displayName = file.name,
                            takenAt = Instant.fromEpochMilliseconds(attrs.lastModifiedTime().toMillis()),
                            uri = file.toUri().toString()
                        )
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: java.io.IOException): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }
            }
        )

        return assets.sortedByDescending { it.takenAt }
    }

    private fun Path.isSupportedImage(): Boolean {
        return extension.lowercase() in setOf("jpg", "jpeg", "png", "heic")
    }

    private fun Path.shouldSkipDirectory(): Boolean {
        val lowerName = name.lowercase()
        return lowerName.endsWith(".photoslibrary") ||
            lowerName.endsWith(".photolibrary") ||
            lowerName.endsWith(".app") ||
            lowerName.endsWith(".bundle") ||
            lowerName in setOf(".git", "node_modules", "build")
    }
}

class DesktopExifPlatform : ExifPlatform {
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        return runCatching {
            JpegGpsReader.readLatLon(asset.path())
        }.getOrNull()
    }

    override suspend fun writeLatLon(asset: Asset, lat: Double, lon: Double): Boolean {
        return false
    }
}

class DesktopGeocoderPlatform : GeocoderPlatform {
    override suspend fun reverseGeocode(lat: Double, lon: Double): String? = null
}

private fun Asset.path(): Path = Paths.get(URI(uri))

private object JpegGpsReader {
    fun readLatLon(path: Path): Pair<Double, Double>? {
        val bytes = Files.readAllBytes(path)
        if (bytes.size < 4 || bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) return null

        var offset = 2
        while (offset + 4 < bytes.size) {
            if (bytes[offset] != 0xFF.toByte()) return null
            val marker = bytes[offset + 1].toInt() and 0xFF
            offset += 2
            if (marker == 0xDA || marker == 0xD9) break
            if (offset + 2 > bytes.size) return null
            val length = bytes.u16(offset, bigEndian = true)
            if (length < 2 || offset + length > bytes.size) return null

            if (marker == 0xE1 && length >= 8 && bytes.matchesAscii(offset + 2, "Exif\u0000\u0000")) {
                return readExif(bytes, offset + 8, length - 8)
            }
            offset += length
        }
        return null
    }

    private fun readExif(bytes: ByteArray, tiffStart: Int, tiffLength: Int): Pair<Double, Double>? {
        if (tiffLength < 8) return null
        val littleEndian = when {
            bytes.matchesAscii(tiffStart, "II") -> true
            bytes.matchesAscii(tiffStart, "MM") -> false
            else -> return null
        }
        if (bytes.u16(tiffStart + 2, !littleEndian) != 42) return null

        val ifd0Offset = bytes.u32(tiffStart + 4, !littleEndian)
        val gpsIfdOffset = findLongTag(bytes, tiffStart, tiffLength, ifd0Offset, 0x8825, littleEndian) ?: return null
        val gps = readGpsIfd(bytes, tiffStart, tiffLength, gpsIfdOffset, littleEndian) ?: return null

        val lat = gps.latitude ?: return null
        val lon = gps.longitude ?: return null
        val signedLat = if (gps.latitudeRef == "S") -lat else lat
        val signedLon = if (gps.longitudeRef == "W") -lon else lon
        return signedLat to signedLon
    }

    private fun findLongTag(
        bytes: ByteArray,
        tiffStart: Int,
        tiffLength: Int,
        ifdOffset: Int,
        tag: Int,
        littleEndian: Boolean
    ): Int? {
        val ifdStart = tiffStart + ifdOffset
        if (ifdStart + 2 > tiffStart + tiffLength) return null
        val count = bytes.u16(ifdStart, !littleEndian)
        repeat(count) { index ->
            val entry = ifdStart + 2 + index * 12
            if (entry + 12 <= tiffStart + tiffLength && bytes.u16(entry, !littleEndian) == tag) {
                return bytes.u32(entry + 8, !littleEndian)
            }
        }
        return null
    }

    private fun readGpsIfd(
        bytes: ByteArray,
        tiffStart: Int,
        tiffLength: Int,
        ifdOffset: Int,
        littleEndian: Boolean
    ): GpsData? {
        val ifdStart = tiffStart + ifdOffset
        if (ifdStart + 2 > tiffStart + tiffLength) return null
        val data = GpsData()
        val count = bytes.u16(ifdStart, !littleEndian)

        repeat(count) { index ->
            val entry = ifdStart + 2 + index * 12
            if (entry + 12 > tiffStart + tiffLength) return@repeat

            val tag = bytes.u16(entry, !littleEndian)
            val type = bytes.u16(entry + 2, !littleEndian)
            val componentCount = bytes.u32(entry + 4, !littleEndian)
            val valueOrOffset = bytes.u32(entry + 8, !littleEndian)

            when (tag) {
                1 -> data.latitudeRef = bytes.asciiValue(entry + 8, componentCount)
                2 -> if (type == 5 && componentCount == 3) {
                    data.latitude = bytes.readDms(tiffStart + valueOrOffset, !littleEndian)
                }
                3 -> data.longitudeRef = bytes.asciiValue(entry + 8, componentCount)
                4 -> if (type == 5 && componentCount == 3) {
                    data.longitude = bytes.readDms(tiffStart + valueOrOffset, !littleEndian)
                }
            }
        }

        return data
    }

    private data class GpsData(
        var latitudeRef: String? = null,
        var latitude: Double? = null,
        var longitudeRef: String? = null,
        var longitude: Double? = null
    )

    private fun ByteArray.readDms(offset: Int, bigEndian: Boolean): Double? {
        if (offset + 24 > size) return null
        val deg = rational(offset, bigEndian) ?: return null
        val min = rational(offset + 8, bigEndian) ?: return null
        val sec = rational(offset + 16, bigEndian) ?: return null
        return deg + min / 60.0 + sec / 3600.0
    }

    private fun ByteArray.rational(offset: Int, bigEndian: Boolean): Double? {
        val numerator = u32(offset, bigEndian)
        val denominator = u32(offset + 4, bigEndian)
        if (denominator == 0) return null
        return numerator.toDouble() / denominator.toDouble()
    }

    private fun ByteArray.asciiValue(offset: Int, count: Int): String {
        val len = count.coerceAtMost(size - offset).coerceAtLeast(0)
        return decodeToString(offset, offset + len).trim('\u0000', ' ')
    }

    private fun ByteArray.matchesAscii(offset: Int, value: String): Boolean {
        if (offset + value.length > size) return false
        return value.indices.all { this[offset + it].toInt().toChar() == value[it] }
    }

    private fun ByteArray.u16(offset: Int, bigEndian: Boolean): Int {
        val a = this[offset].toInt() and 0xFF
        val b = this[offset + 1].toInt() and 0xFF
        return if (bigEndian) (a shl 8) or b else (b shl 8) or a
    }

    private fun ByteArray.u32(offset: Int, bigEndian: Boolean): Int {
        val a = this[offset].toInt() and 0xFF
        val b = this[offset + 1].toInt() and 0xFF
        val c = this[offset + 2].toInt() and 0xFF
        val d = this[offset + 3].toInt() and 0xFF
        return if (bigEndian) {
            (a shl 24) or (b shl 16) or (c shl 8) or d
        } else {
            (d shl 24) or (c shl 16) or (b shl 8) or a
        }
    }
}
