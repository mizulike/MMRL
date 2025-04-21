package com.dergoogler.mmrl.webui.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Manager(
    val name: String,
    val versionName: String,
    val versionCode: Int,
)
