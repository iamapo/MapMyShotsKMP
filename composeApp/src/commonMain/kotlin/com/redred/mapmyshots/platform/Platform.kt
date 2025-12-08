package com.redred.mapmyshots.platform

expect class Platform {
    val name: String
}

expect fun getPlatform(): Platform
