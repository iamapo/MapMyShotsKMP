package com.redred.mapmyshots

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
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

    private var onPermissionsGranted: (() -> Unit)? = null

    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onPermissionsResult
    )

    private val requestUserConsent = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
        ::onUserConsentResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(androidModule, sharedModule)
            }
        }

        AndroidDeleteCoordinator.attach { intentSender ->
            requestUserConsent.launch(IntentSenderRequest.Builder(intentSender).build())
        }

        setContent {
            MaterialTheme {
                Surface {
                    App(onRequestPermissions = ::requestMissingPermissions)
                }
            }
        }
    }

    override fun onDestroy() {
        AndroidDeleteCoordinator.detach()
        super.onDestroy()
    }

    /** Requests only missing permissions; [onGranted] runs once the user granted photo access. */
    private fun requestMissingPermissions(onGranted: () -> Unit) {
        val missing = requiredPermissions().filter { permission ->
            checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) return
        onPermissionsGranted = onGranted
        requestPerms.launch(missing.toTypedArray())
    }

    private fun onPermissionsResult(result: Map<String, Boolean>) {
        if (result.values.any { it }) {
            onPermissionsGranted?.invoke()
        }
        onPermissionsGranted = null
    }

    private fun onUserConsentResult(result: ActivityResult) {
        AndroidDeleteCoordinator.onUserConsentResult(result.resultCode == Activity.RESULT_OK)
    }

    private fun requiredPermissions(): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= 33) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
    }
}
