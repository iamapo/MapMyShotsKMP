package com.redred.mapmyshots

import android.content.IntentSender
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AndroidDeleteCoordinator {
    private var launcher: ((IntentSender) -> Unit)? = null
    private var pendingContinuation: CancellableContinuation<Boolean>? = null

    fun attach(launcher: (IntentSender) -> Unit) {
        this.launcher = launcher
    }

    fun detach() {
        launcher = null
    }

    suspend fun requestDeletePermission(intentSender: IntentSender): Boolean =
        suspendCancellableCoroutine { continuation ->
            val currentLauncher = launcher
            if (currentLauncher == null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            synchronized(this) {
                if (pendingContinuation != null) {
                    continuation.resume(false)
                    return@suspendCancellableCoroutine
                }
                pendingContinuation = continuation
            }

            continuation.invokeOnCancellation {
                synchronized(this) {
                    if (pendingContinuation === continuation) {
                        pendingContinuation = null
                    }
                }
            }

            currentLauncher(intentSender)
        }

    fun onDeletePermissionResult(granted: Boolean) {
        val continuation = synchronized(this) {
            pendingContinuation.also { pendingContinuation = null }
        }
        continuation?.resume(granted)
    }
}
