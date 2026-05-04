package com.redred.mapmyshots

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.redred.mapmyshots.di.desktopModule
import com.redred.mapmyshots.di.sharedModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(sharedModule, desktopModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "MapMyShots"
    ) {
        App()
    }
}
