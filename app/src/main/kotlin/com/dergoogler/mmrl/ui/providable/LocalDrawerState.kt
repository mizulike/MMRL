package com.dergoogler.mmrl.ui.providable

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDrawerState = staticCompositionLocalOf<DrawerState> {
    error("CompositionLocal NavController not present")
}