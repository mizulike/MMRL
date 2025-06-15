package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility

open class SukiSUModuleManager() : KernelSUModuleManager() {
    override fun getManagerName(): String = "SukiSU"

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = false,
        canRestoreModules = false
    )
}