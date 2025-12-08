package com.redred.mapmyshots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.redred.mapmyshots.model.Asset
import com.redred.mapmyshots.ui.PhotoDetailsScreen
import com.redred.mapmyshots.ui.PhotoListScreen

@Composable
fun App(
    onRequestPermissions: (() -> Unit)? = null,
    listScreen: @Composable ((Asset) -> Unit) -> Unit = { onOpen -> PhotoListScreen(onOpen) }
) {
    LaunchedEffect(Unit) { onRequestPermissions?.invoke() }

    val current = remember { mutableStateOf<Asset?>(null) }
    if (current.value == null) {
        listScreen { asset -> current.value = asset }
    } else {
        PhotoDetailsScreen(photo = current.value!!, onSaved = { current.value = null })
    }

    //MaterialTheme {
    //   Text("Hallo, MapMyShots läuft 🎉")
    //}
}