package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState

val LocalModule = staticCompositionLocalOf<OnlineModule> {
    error("CompositionLocal OnlineModule not present")
}


val LocalModuleState = staticCompositionLocalOf<OnlineState> {
    error("CompositionLocal OnlineState not present")
}