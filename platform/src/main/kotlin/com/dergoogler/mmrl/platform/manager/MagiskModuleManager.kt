package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.stub.IShell
import com.dergoogler.mmrl.platform.stub.IShellCallback

open class MagiskModuleManager(
    fileManager: FileManager,
) : BaseModuleManager(
    fileManager = fileManager
) {
    override fun getManagerName(): String = "Magisk"

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = true,
        canRestoreModules = true
    )

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int = mVersionCode

    override fun isSafeMode(): Boolean = false
    override fun isLkmMode(): NullableBoolean = NullableBoolean(null)

    override fun setSuEnabled(enabled: Boolean): Boolean = true
    override fun isSuEnabled(): Boolean = true

    override fun getSuperUserCount(): Int = -1

    override fun uidShouldUmount(uid: Int): Boolean = false

    override fun enable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) callback.onFailure(id, null)

        runCatching {
            dir.resolve("remove").apply { if (exists()) delete() }
            dir.resolve("disable").apply { if (exists()) delete() }
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun disable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) return callback.onFailure(id, null)

        runCatching {
            dir.resolve("remove").apply { if (exists()) delete() }
            dir.resolve("disable").createNewFile()
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun remove(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) return callback.onFailure(id, null)

        runCatching {
            dir.resolve("disable").apply { if (exists()) delete() }
            dir.resolve("remove").createNewFile()
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun action(modId: String, legacy: Boolean, callback: IShellCallback): IShell {
        val env = mutableMapOf(
            "ASH_STANDALONE" to "1",
            "MAGISK" to "true",
            "MAGISK_VER" to version,
            "MAGISK_VER_CODE" to versionCode.toString(),
            "MAGISKTMP" to "null"
        )

        return action(
            cmd = listOf("busybox", "sh", "/data/adb/modules/$modId/action.sh"),
            env = env,
            callback = callback
        )
    }

    override fun install(
        path: String,
        bulkModules: List<BulkModule>,
        callback: IShellCallback,
    ): IShell =
        install(
            cmd = listOf("magisk", "--install-module", path),
            path = path,
            bulkModules = bulkModules,
            callback = callback
        )
}