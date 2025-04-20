package com.dergoogler.mmrl.utils

import java.net.URI
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

val Float.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

val Long.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }
