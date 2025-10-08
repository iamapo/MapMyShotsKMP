package com.redred.mapmyshots

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.redred.mapmyshots.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {

    private val requestPerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* you could react if needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(androidModule)
        }

        setContent {
            MaterialTheme {
                Surface {
                    AppRoot(
                        onRequestPermissions = {
                            val perms = if (Build.VERSION.SDK_INT >= 33) arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES
                            ) else arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            Log.d("MainActivity", "onRequestPermissions: $perms")
                            requestPerms.launch(perms)
                        }
                    )
                }
            }
        }
    }
}