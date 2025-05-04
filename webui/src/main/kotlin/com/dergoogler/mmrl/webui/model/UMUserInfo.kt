package com.dergoogler.mmrl.webui.model

import android.webkit.JavascriptInterface
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
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