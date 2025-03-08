package com.dergoogler.mmrl.datastore.model

import kotlinx.serialization.Serializable

@Serializable
data class RepositoriesMenu(
    val option: Option = Option.Name,
    val descending: Boolean = false,
    val showModulesCount: Boolean = true,
    val showUpdatedTime: Boolean = true,
    val showCover: Boolean = true,
)