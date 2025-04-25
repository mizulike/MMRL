package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.stub.IShell
import com.dergoogler.mmrl.platform.stub.IShellCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class KsuNextModuleManager(
    fileManager: FileManager,
) : KernelSUModuleManager(
    fileManager = fileManager
) {
    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = false,
        canRestoreModules = true
    )

    override fun enable(id: String, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = modulesDir.resolve(id)
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "ksud module restore $id && ksud module enable $id".submit {
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

    override fun action(modId: String, legacy: Boolean, callback: IShellCallback): IShell =
        if (legacy) {
            val env = mutableMapOf(
                "ASH_STANDALONE" to "1",
                "KSU" to "true",
                "KSU_NEXT" to "true",
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
}