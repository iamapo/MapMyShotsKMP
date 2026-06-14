package com.redred.mapmyshots.di

import com.redred.mapmyshots.platform.DesktopExifPlatform
import com.redred.mapmyshots.platform.DesktopGeocoderPlatform
import com.redred.mapmyshots.platform.DesktopPhotoRepository
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.PhotoRepository
import org.koin.dsl.module

val desktopModule = module {
    single<PhotoRepository> { DesktopPhotoRepository() }
    single<ExifPlatform> { DesktopExifPlatform() }
    single<GeocoderPlatform> { DesktopGeocoderPlatform() }
}
