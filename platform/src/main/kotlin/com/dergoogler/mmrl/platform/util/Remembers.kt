package com.dergoogler.mmrl.platform.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull


@Composable
inline fun <T> waitOfPlatform(
    key: Any? = Unit,
    crossinline block: @DisallowComposableCalls suspend () -> T,
): MutableState<T?> = waitOfPlatform(null, key, block)

@Composable
inline fun <T> waitOfPlatform(
    fallback: T,
    key: Any? = Unit,
    crossinline block: @DisallowComposableCalls suspend () -> T,
): MutableState<T> {
    val state = remember(key, Platform.isAlive) { mutableStateOf(fallback) }

    LaunchedEffect(key, Platform.isAlive) {
        withTimeoutOrNull(TIMEOUT_MILLIS) {
            while (!Platform.isAlive) {
                delay(500)
            }
        } ?: return@LaunchedEffect

        state.value = block()
    }

    return state
}