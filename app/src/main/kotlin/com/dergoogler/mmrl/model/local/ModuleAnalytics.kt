package com.dergoogler.mmrl.model.local

data class ModuleAnalytics(
    val totalModules: Int,
    val totalEnabled: Int,
    val totalDisabled: Int,
    val totalUpdated: Int,
    val totalModulesUsage: Long,
    val totalDeviceStorage: Long,
    val totalStorageUsage: Float,
)