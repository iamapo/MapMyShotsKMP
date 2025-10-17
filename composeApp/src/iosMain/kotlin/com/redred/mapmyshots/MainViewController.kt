package com.redred.mapmyshots

import androidx.compose.ui.window.ComposeUIViewController
import com.redred.mapmyshots.di.iosModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    try {
        startKoin {
            modules(iosModule)
        }
    } catch (_: IllegalStateException) {
        // bereits gestartet
    }

    App(onRequestPermissions = null)
}