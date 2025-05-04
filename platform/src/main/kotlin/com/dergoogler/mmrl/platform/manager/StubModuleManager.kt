package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback

open class StubModuleManager(
    fileManager: FileManager,
) : BaseModuleManager(
    fileManager = fileManager
) {
    override fun getManagerName(): String = "Non-Root"
    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = false,
        canRestoreModules = false
    )
    override fun getVersion(): String = "null"
    override fun getVersionCode(): Int = -1
    override fun isSafeMode(): Boolean = false
    override fun isLkmMode(): NullableBoolean = NullableBoolean(null)
    override fun setSuEnabled(enabled: Boolean): Boolean = false
    override fun isSuEnabled(): Boolean = false
    override fun getSuperUserCount(): Int = -1
    override fun uidShouldUmount(uid: Int): Boolean = false
    override fun enable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {}
    override fun disable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {}
    override fun remove(id: String, useShell: Boolean, callback: IModuleOpsCallback) {}
    override fun getInstallCommand(path: String): String = "exit 1"
    override fun getActionCommand(id: ModId): String = "exit 1"
    override fun getActionEnvironment(): List<String> = emptyList()
}