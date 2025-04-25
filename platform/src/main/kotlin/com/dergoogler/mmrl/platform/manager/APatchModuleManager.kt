package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.stub.IShell
import com.dergoogler.mmrl.platform.stub.IShellCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class APatchModuleManager(
    val fileManager: FileManager,
) : BaseModuleManager(
    fileManager = fileManager
) {
    override fun getManagerName(): String = "APatch"

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int = mVersionCode

    override fun isSafeMode(): Boolean = false
    override fun isLkmMode(): NullableBoolean = NullableBoolean(null)

    override fun setSuEnabled(enabled: Boolean): Boolean = true
    override fun isSuEnabled(): Boolean = true

    override fun getSuperUserCount(): Int = -1

    override fun uidShouldUmount(uid: Int): Boolean = false

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = fileManager.exists("/data/adb/.bind_mount_enable") && (versionCode >= 11011 && !fileManager.exists(
            "/data/adb/.overlay_enable"
        )),
        canRestoreModules = false
    )

    override fun enable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "apd module enable $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                dir.resolve("remove").apply { if (exists()) delete() }
                dir.resolve("disable").apply { if (exists()) delete() }
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun disable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) return callback.onFailure(id, null)

        if (useShell) {
            "apd module disable $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                dir.resolve("remove").apply { if (exists()) delete() }
                dir.resolve("disable").createNewFile()
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun remove(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) return callback.onFailure(id, null)

        if (useShell) {
            "apd module uninstall $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                dir.resolve("disable").apply { if (exists()) delete() }
                dir.resolve("remove").createNewFile()
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun action(modId: String, legacy: Boolean, callback: IShellCallback): IShell =
        if (legacy) {
            val env = mutableMapOf(
                "ASH_STANDALONE" to "1",
                "APATCH" to "true",
                "APATCH_VER" to version,
                "APATCH_VER_CODE" to versionCode.toString(),
            )

            action(
                cmd = listOf("busybox", "sh", "/data/adb/modules/$modId/action.sh"),
                env = env,
                callback = callback
            )
        } else {
            action(
                cmd = listOf("apd", "module", "action", modId),
                callback = callback
            )
        }


    override fun install(
        path: String,
        bulkModules: List<BulkModule>,
        callback: IShellCallback,
    ): IShell = install(
        cmd = listOf("apd", "module", "install", path),
        path = path,
        bulkModules = bulkModules,
        callback = callback
    )
}