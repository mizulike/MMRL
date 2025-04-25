package com.dergoogler.mmrl.platform.manager

import android.os.Build
import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.content.LocalModuleFeatures
import com.dergoogler.mmrl.platform.content.State
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IShell
import com.dergoogler.mmrl.platform.stub.IShellCallback
import com.dergoogler.mmrl.platform.util.Shell.exec
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File

abstract class BaseModuleManager(
    private val fileManager: IFileManager,
) : IModuleManager.Stub() {
    internal val modulesDir = File(MODULES_PATH)

    internal val mVersion by lazy {
        "su -v".exec().getOrDefault("unknown")
    }

    internal val mVersionCode by lazy {
        "su -V".exec().getOrDefault("").toIntOr(-1)
    }

    override fun reboot(reason: String) {
        if (reason == "recovery") {
            "/system/bin/input keyevent 26".exec()
        }

        "/system/bin/svc power reboot $reason || /system/bin/reboot $reason".exec()
    }

    override fun getModules() = modulesDir.listFiles()
        .orEmpty()
        .mapNotNull { dir ->
            readProps(dir)?.toModule(dir)
        }

    private fun hasWebUI(id: String): Boolean {
        val moduleDir = modulesDir.resolve(id)
        val webroot = moduleDir.resolve(WEBROOT_PATH)
        return webroot.exists() && webroot.isDirectory
    }

    private fun hasFeature(type: String, id: String): Boolean {
        val moduleDir = modulesDir.resolve(id)
        val feature = moduleDir.resolve(type)
        return feature.exists() && feature.isFile
    }

    override fun getModuleById(id: String): LocalModule? {
        val dir = modulesDir.resolve(id)
        return readProps(dir)?.toModule(dir)
    }

    override fun getModuleInfo(zipPath: String): LocalModule? {
        val zipFile = ZipFile.Builder().setFile(zipPath).get()
        val entry = zipFile.getEntry(PROP_FILE) ?: return null

        return zipFile.getInputStream(entry).use {
            it.bufferedReader()
                .readText()
                .let(::readProps)
                .toModule()
        }
    }

    private fun readProps(props: String) = props.lines()
        .associate { line ->
            val items = line.split("=", limit = 2).map { it.trim() }
            if (items.size != 2) {
                "" to ""
            } else {
                items[0] to items[1]
            }
        }

    private fun readProps(moduleDir: File) =
        moduleDir.resolve(PROP_FILE).let {
            when {
                it.exists() -> readProps(it.readText())
                else -> null
            }
        }

    private fun readState(moduleDir: File): State {
        moduleDir.resolve("remove").apply {
            if (exists()) return State.REMOVE
        }

        moduleDir.resolve("disable").apply {
            if (exists()) return State.DISABLE
        }

        moduleDir.resolve("update").apply {
            if (exists()) return State.UPDATE
        }

        return State.ENABLE
    }

    private fun readLastUpdated(moduleDir: File): Long {
        MODULE_FILES.forEach { filename ->
            val file = moduleDir.resolve(filename)
            if (file.exists()) {
                return file.lastModified()
            }
        }

        return 0L
    }

    private fun Map<String, String>.toModule(
        dir: File,
    ): LocalModule {
        val id = getOrDefault("id", dir.name)

        return toModule(
            path = dir.name,
            state = readState(dir),
            features = LocalModuleFeatures(
                webui = hasWebUI(id),
                action = hasFeature(ACTION_FILE, id),
                service = hasFeature(SERVICE_FILE, id),
                postFsData = hasFeature(POST_FS_DATA_FILE, id),
                postMount = hasFeature(POST_MOUNT_FILE, id),
                resetprop = hasFeature(SYSTEM_PROP_FILE, id),
                bootCompleted = hasFeature(BOOT_COMPLETED_FILE, id),
                sepolicy = hasFeature(SE_POLICY, id),
                zygisk = false,
                apks = false
            ),
            size = fileManager.size(dir.path, true),
            lastUpdated = readLastUpdated(dir)
        )
    }

    private fun Map<String, String>.toModule(
        path: String = "unknown",
        state: State = State.ENABLE,
        lastUpdated: Long = 0L,
        size: Long = 0L,
        features: LocalModuleFeatures = LocalModuleFeatures.EMPTY,
    ) = LocalModule(
        id = getOrDefault("id", path),
        name = getOrDefault("name", path),
        version = getOrDefault("version", ""),
        versionCode = getOrDefault("versionCode", "-1").toIntOr(-1),
        author = getOrDefault("author", ""),
        description = getOrDefault("description", ""),
        updateJson = getOrDefault("updateJson", ""),
        state = state,
        features = features,
        size = size,
        lastUpdated = lastUpdated
    )

    private fun String.toIntOr(defaultValue: Int) =
        runCatching {
            toInt()
        }.getOrDefault(defaultValue)

    internal fun install(
        cmd: List<String>,
        path: String,
        bulkModules: List<BulkModule>,
        callback: IShellCallback,
        env: Map<String, String> = emptyMap(),
        versionCode: Int = -1,
        versionName: String = "unknown",
    ): IShell {
        val mEnv = mutableMapOf(
            "MMRL" to "true",
            "MMRL_VER" to versionName,
            "MMRL_VER_CODE" to versionCode.toString(),
            "BULK_MODULES" to bulkModules.joinToString(" ") { it.id },
        )

        mEnv.putAll(env)

        val module = getModuleInfo(path)

        return getShell(cmd, mEnv, module, callback)
    }

    internal fun action(
        cmd: List<String>,
        callback: IShellCallback,
        env: Map<String, String> = emptyMap(),
        versionCode: Int = -1,
        versionName: String = "unknown",
    ): IShell {
        val mEnv = mutableMapOf(
            "MMRL" to "true",
            "MMRL_VER" to versionName,
            "MMRL_VER_CODE" to versionCode.toString(),
            "BOOTMODE" to "true",
            "ARCH" to Build.SUPPORTED_ABIS[0],
            "API" to Build.VERSION.SDK_INT.toString(),
            "IS64BIT" to Build.SUPPORTED_64_BIT_ABIS.isNotEmpty().toString()
        )

        mEnv.putAll(env)


        return this.getShell(cmd, mEnv, null, callback)
    }

    override fun getShell(
        command: List<String>,
        env: Map<String, String>,
        module: LocalModule?,
        callback: IShellCallback,
    ): IShell =
        object : IShell.Stub() {
            val pid = com.dergoogler.mmrl.platform.util.Shell.nativeCreateShell()

            override fun isAlive(): Boolean =
                com.dergoogler.mmrl.platform.util.Shell.nativeIsAlive(pid)

            override fun exec() {
                com.dergoogler.mmrl.platform.util.Shell.nativeExec(
                    pid,
                    command.toTypedArray(),
                    module,
                    callback,
                    env
                )
            }

            override fun close() = com.dergoogler.mmrl.platform.util.Shell.nativeClose(pid)
        }

    companion object {
        const val PROP_FILE = "module.prop"
        const val WEBROOT_PATH = "webroot"
        const val MODULES_PATH = "/data/adb/modules"

        const val ACTION_FILE = "action.sh"
        const val BOOT_COMPLETED_FILE = "boot-completed.sh"
        const val SERVICE_FILE = "service.sh"
        const val POST_FS_DATA_FILE = "post-fs-data.sh"
        const val POST_MOUNT_FILE = "post-mount.sh"
        const val SYSTEM_PROP_FILE = "system.prop"
        const val SE_POLICY = "sepolicy.rule"


        val MODULE_SERVICE_FILES = listOf(
            ACTION_FILE,
            SERVICE_FILE,
            POST_FS_DATA_FILE,
            POST_MOUNT_FILE,
            WEBROOT_PATH,
            BOOT_COMPLETED_FILE
        )
        val MODULE_FILES = listOf(
            SE_POLICY,
            *MODULE_SERVICE_FILES.toTypedArray(),
            "uninstall.sh",
            "system", "module.prop",
        )
    }
}