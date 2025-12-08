package com.redred.mapmyshots.di

import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.service.ExifService
import com.redred.mapmyshots.service.PhotoService
import com.redred.mapmyshots.service.SimilarityService
import com.redred.mapmyshots.viewmodel.PhotoDetailsViewModel
import com.redred.mapmyshots.viewmodel.PhotoListViewModel
import org.koin.dsl.module

val sharedModule = module {

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