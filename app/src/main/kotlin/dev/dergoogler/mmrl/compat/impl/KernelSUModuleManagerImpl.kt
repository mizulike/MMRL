package dev.dergoogler.mmrl.compat.impl

import com.topjohnwu.superuser.Shell
import dev.dergoogler.mmrl.compat.content.AppProfile
import dev.dergoogler.mmrl.compat.content.BulkModule
import dev.dergoogler.mmrl.compat.content.ModuleCompatibility
import dev.dergoogler.mmrl.compat.content.NullableBoolean
import dev.dergoogler.mmrl.compat.impl.ksu.KsuNative
import dev.dergoogler.mmrl.compat.impl.ksu.getKernelVersion
import dev.dergoogler.mmrl.compat.stub.IModuleOpsCallback
import dev.dergoogler.mmrl.compat.stub.IShell
import dev.dergoogler.mmrl.compat.stub.IShellCallback

internal open class KernelSUModuleManagerImpl(
    shell: Shell,
    seLinuxContext: String,
    fileManager: FileManagerImpl,
) : BaseModuleManagerImpl(
    shell = shell,
    seLinuxContext = seLinuxContext,
    fileManager = fileManager
) {
    override fun getManagerName(): String = "KernelSU"

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int = KsuNative.getVersion()

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

    override fun setAppProfile(profile: AppProfile?): Boolean = KsuNative.setAppProfile(profile)
    override fun getAppProfile(key: String?, uid: Int): AppProfile? =
        KsuNative.getAppProfile(key, uid)

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
                if (it.isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, it.out.joinToString())
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
                if (it.isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, it.out.joinToString())
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
                if (it.isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, it.out.joinToString())
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
            val cmds = arrayOf(
                "export ASH_STANDALONE=1",
                "export KSU=true",
                "export KSU_VER=${version}",
                "export KSU_VER_CODE=${versionCode}",
                "busybox sh /data/adb/modules/$modId/action.sh"
            )

            action(
                cmd = cmds,
                callback = callback
            )
        } else {
            action(
                cmd = arrayOf("ksud module action $modId"),
                callback = callback
            )
        }


    override fun install(
        path: String,
        bulkModules: List<BulkModule>,
        callback: IShellCallback,
    ): IShell = install(
        cmd = "ksud module install '${path}'",
        path = path,
        bulkModules = bulkModules,
        callback = callback
    )
}