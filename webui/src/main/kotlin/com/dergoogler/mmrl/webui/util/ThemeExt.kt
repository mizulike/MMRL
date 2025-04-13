package com.dergoogler.mmrl.webui.util

import androidx.compose.ui.graphics.Color

fun Color.toCssValue(): String {
    return "#${red.toHex()}${green.toHex()}${blue.toHex()}${alpha.toHex()}"
}

fun Float.toHex(): String {
    return (this * 255).toInt().coerceIn(0, 255).toString(16).padStart(2, '0')
}
