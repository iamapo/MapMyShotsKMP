package com.redred.mapmyshots.di

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.AndroidExifPlatform
import com.redred.mapmyshots.platform.AndroidGeocoderPlatform
import com.redred.mapmyshots.platform.AndroidPhotoRepository
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.service.SimilarityService
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val androidModule = module {
    single<PhotoRepository> { AndroidPhotoRepository(androidContext()) }
    single<ExifPlatform> { AndroidExifPlatform(androidContext()) }
    single<GeocoderPlatform> { AndroidGeocoderPlatform(androidContext()) }

    single { ExifService(get()) }
    single { PhotoService(get(), get()) }
    single { SimilarityService(get(), get()) }

    factory { (photo: Asset) ->
        PhotoDetailsViewModel(
            photo = photo,
            exif = get(),
            sim = get(),
            geocoder = get()
        )
    }

    factory { PhotoListViewModel(get()) }
}