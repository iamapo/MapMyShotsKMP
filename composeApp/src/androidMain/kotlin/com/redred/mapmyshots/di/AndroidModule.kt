package com.redred.mapmyshots.di

import com.redred.mapmyshots.platform.AndroidExifPlatform
import com.redred.mapmyshots.platform.AndroidGeocoderPlatform
import com.redred.mapmyshots.platform.AndroidPhotoRepository
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.PhotoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<PhotoRepository> { AndroidPhotoRepository(androidContext()) }
    single<ExifPlatform> { AndroidExifPlatform(androidContext()) }
    single<GeocoderPlatform> { AndroidGeocoderPlatform(androidContext()) }
}