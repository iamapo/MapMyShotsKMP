package com.redred.mapmyshots.platform

import com.redred.mapmyshots.model.Asset

class IosExifPlatform : ExifPlatform {
    override suspend fun readLatLon(asset: Asset): Pair<Double, Double>? {
        TODO("Not yet implemented")
    }

    override suspend fun writeLatLon(
        asset: Asset,
        lat: Double,
        lon: Double
    ): Boolean {
        TODO("Not yet implemented")
    }

}
