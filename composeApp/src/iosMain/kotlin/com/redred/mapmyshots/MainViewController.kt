package com.redred.mapmyshots

import androidx.compose.ui.window.ComposeUIViewController
import com.redred.mapmyshots.di.iosModule
import com.redred.mapmyshots.di.sharedModule
import org.koin.core.context.startKoin

private var koinStarted = false

fun MainViewController() = ComposeUIViewController {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }

    App()
}

fun initKoin() {
    startKoin {
        modules(
            sharedModule,
            iosModule
        )
    }
}
