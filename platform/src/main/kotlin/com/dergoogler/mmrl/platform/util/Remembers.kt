package com.dergoogler.mmrl.platform.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.collectLatest
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import com.dergoogler.mmrl.platform.PlatformManager

@ExperimentalComposeApi
@Composable
inline fun <T> waitOfPlatform(
    fallback: T? = null,
    key1: Any? = null,
    key2: Any? = null,
    crossinline block: @DisallowComposableCalls suspend () -> T,
): State<T?> {
    val state = remember(key1, key2) { mutableStateOf(fallback) }

    LaunchedEffect(key1, key2, PlatformManager) {
        PlatformManager.isAliveFlow.collectLatest { isAlive ->
            if (isAlive) {
                Log.d(PlatformManager.TAG, "waitOfPlatform: Platform is alive, executing block.")
                try {
                    val result = withTimeoutOrNull(TIMEOUT_MILLIS) {
                        block()
                    }
                    if (result != null) {
                        state.value = result
                    } else {
                        Log.w(PlatformManager.TAG, "waitOfPlatform: Block execution timed out.")
                    }
                } catch (e: Exception) {
                    Log.e(PlatformManager.TAG, "waitOfPlatform: Error executing block.", e)
                }
            } else {
                Log.d(
                    PlatformManager.TAG,
                    "waitOfPlatform: Platform is not alive yet, state remains fallback."
                )
            }
        }
    }

    return state
}
