package com.dergoogler.webui.plugin

data class Instance(
    val instance: Any,
    val name: String,
    val targetModules: List<String> = emptyList(),
)