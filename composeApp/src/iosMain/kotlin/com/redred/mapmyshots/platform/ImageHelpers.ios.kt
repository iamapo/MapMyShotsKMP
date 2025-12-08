package com.redred.mapmyshots.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import org.jetbrains.skia.Image

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toImageBitmap(): ImageBitmap {
    val data = UIImagePNGRepresentation(this) ?: return ImageBitmap(1, 1)

    val bytes = ByteArray(data.length.toInt())
    bytes.usePinned {
        memcpy(it.addressOf(0), data.bytes, data.length)
    }

    val skiaImage = Image.makeFromEncoded(bytes)
    return skiaImage.toComposeImageBitmap()
}