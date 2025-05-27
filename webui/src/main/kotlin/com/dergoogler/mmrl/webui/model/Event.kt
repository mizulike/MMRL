package com.dergoogler.mmrl.webui.model

import com.dergoogler.mmrl.webui.util.PostWindowEventMessage
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WXEventHandler(
    val type: PostWindowEventMessage? = null
)
