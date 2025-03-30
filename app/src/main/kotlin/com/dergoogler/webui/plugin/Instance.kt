package com.dergoogler.webui.plugin

import androidx.annotation.Keep

@Keep
data class Instance(
    val instance: Any,
    val name: String,
    val targetModules: List<String> = emptyList(),
)