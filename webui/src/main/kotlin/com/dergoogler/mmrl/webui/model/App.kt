package com.dergoogler.mmrl.webui.model

import android.webkit.JavascriptInterface

data class App(
    @get:JavascriptInterface
    val packageName: String,
    @get:JavascriptInterface
    val versionName: String,
    @get:JavascriptInterface
    val versionCode: Long,
)