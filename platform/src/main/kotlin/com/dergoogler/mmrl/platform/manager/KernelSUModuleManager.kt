package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.platform.ksu.getKernelVersion
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.stub.IShell
import com.dergoogler.mmrl.platform.stub.IShellCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class KernelSUModuleManager(
    fileManager: FileManager,
) : BaseModuleManager(
    fileManager = fileManager
) {
    override fun getManagerName(): String = "KernelSU"

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int {
        val ksuVersion = KsuNative.getVersion()

        return if (ksuVersion != -1) {
            ksuVersion
        } else {
            mVersionCode
        }
    }

    override fun setSuEnabled(enabled: Boolean): Boolean = KsuNative.setSuEnabled(enabled)
    override fun isSuEnabled(): Boolean = KsuNative.isSuEnabled()

    override fun isLkmMode(): NullableBoolean = with(KsuNative) {
        val kernelVersion = getKernelVersion()
        val ksuVersion = getVersion()

        return NullableBoolean(
            if (ksuVersion >= MINIMAL_SUPPORTED_KERNEL_LKM && kernelVersion.isGKI()) {
                isLkmMode()
            } else {
                null
            }
        )
    }

    override fun getSuperUserCount(): Int = KsuNative.getAllowList().size

    override fun isSafeMode(): Boolean = KsuNative.isSafeMode()

    override fun uidShouldUmount(uid: Int): Boolean = KsuNative.uidShouldUmount(uid)

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = false,
        canRestoreModules = false
    )

    override fun enable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "ksud module enable $id".submit {
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
            "ksud module disable $id".submit {
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
            "ksud module uninstall $id".submit {
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
                "KSU" to "true",
                "KSU_VER" to version,
                "KSU_VER_CODE" to versionCode.toString(),
            )

            action(
                cmd = listOf("busybox", "sh", "/data/adb/modules/$modId/action.sh"),
                env = env,
                callback = callback
            )
        } else {
            action(
                cmd = listOf("ksud", "module", "action", modId),
                callback = callback
            )
        }


    override fun install(
        path: String,
        bulkModules: List<BulkModule>,
        callback: IShellCallback,
    ): IShell = install(
        cmd = listOf("ksud", "module", "install", path),
        path = path,
        bulkModules = bulkModules,
        callback = callback
    )
}