package com.dergoogler.mmrl.webui.model

import android.webkit.JavascriptInterface

data class UMUserInfo(
    @get:JavascriptInterface
    val name: String,
    @get:JavascriptInterface
    val id: Int,
    @get:JavascriptInterface
    val isPrimary: Boolean,
    @get:JavascriptInterface
    val isAdmin: Boolean,
    @get:JavascriptInterface
    val isEnabled: Boolean
)