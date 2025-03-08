package com.dergoogler.mmrl.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class ModulesMenu(
    val option: Option = Option.Name,
    val descending: Boolean = false,
    val pinEnabled: Boolean = true,
    val pinAction: Boolean = false,
    val pinWebUI: Boolean = false,
    val showUpdatedTime: Boolean = true
)