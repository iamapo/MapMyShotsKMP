package com.redred.mapmyshots.di

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.platform.ExifPlatform
import com.redred.mapmyshots.platform.GeocoderPlatform
import com.redred.mapmyshots.platform.IosExifPlatform
import com.redred.mapmyshots.platform.IosGeocoderPlatform
import com.redred.mapmyshots.platform.IosPhotoRepository
import com.redred.mapmyshots.platform.PhotoRepository
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.service.SimilarityService
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.dsl.module

val iosModule = module {
    single<PhotoRepository> { IosPhotoRepository() }
    single<ExifPlatform> { IosExifPlatform() }
    single<GeocoderPlatform> { IosGeocoderPlatform() }

    single { ExifService(get()) }
    single { PhotoService(get(), get()) }
    single { SimilarityService(get(), get()) }

    factory { (photo: Asset) ->
        PhotoDetailsViewModel(photo = photo, exif = get(), sim = get(), geocoder = get())
    }
    factory { PhotoListViewModel(get()) }
}