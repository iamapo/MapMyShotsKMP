package com.redred.mapmyshots.platform

actual class Platform {
    actual val name: String = "Desktop ${System.getProperty("os.name")}"
}

actual fun getPlatform(): Platform = Platform()
