package com.redred.mapmyshots.di

import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.IOSExifPlatform
import com.redred.mapmyshots.platform.IOSGeocoderPlatform
import com.redred.mapmyshots.platform.IOSPhotoRepository
import com.redred.mapmyshots.platform.PhotoRepository
import org.koin.dsl.module

val iosModule = module {
    single<PhotoRepository> { IOSPhotoRepository() }
    single<ExifPlatform> { IOSExifPlatform() }
    single<GeocoderPlatform> { IOSGeocoderPlatform() }
}