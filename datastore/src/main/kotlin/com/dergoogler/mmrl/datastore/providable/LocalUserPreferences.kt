package com.dergoogler.mmrl.datastore.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.datastore.model.UserPreferences

val LocalUserPreferences = staticCompositionLocalOf { UserPreferences() }
