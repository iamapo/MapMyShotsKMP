package com.redred.mapmyshots

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.redred.mapmyshots.di.androidModule
import com.redred.mapmyshots.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {

    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* you could react if needed */ }

    private val requestDeletePermission = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        AndroidDeleteCoordinator.onDeletePermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(androidModule, sharedModule)
            }
        }

        AndroidDeleteCoordinator.attach { intentSender ->
            requestDeletePermission.launch(IntentSenderRequest.Builder(intentSender).build())
        }

        setContent {
            MaterialTheme {
                Surface {
                    App(
                        onRequestPermissions = {
                            val perms = buildList {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    add(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                if (Build.VERSION.SDK_INT >= 29) {
                                    add(Manifest.permission.ACCESS_MEDIA_LOCATION)
                                }
                            }.toTypedArray()
                            requestPerms.launch(perms)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        AndroidDeleteCoordinator.detach()
        super.onDestroy()
    }
}
