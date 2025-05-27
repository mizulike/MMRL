package com.dergoogler.mmrl.webui.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WXEventHandler(
    val type: String
)
