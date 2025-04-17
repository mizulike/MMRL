package com.dergoogler.mmrl.platform.service

import android.os.SELinux
import android.system.Os
import com.dergoogler.mmrl.platform.Compat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.manager.APatchModuleManager
import com.dergoogler.mmrl.platform.manager.KernelSUModuleManager
import com.dergoogler.mmrl.platform.manager.KsuNextModuleManager
import com.dergoogler.mmrl.platform.manager.MagiskModuleManager
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import kotlin.system.exitProcess

internal class ServiceManager(
    private val platform: Platform,
) : IServiceManager.Stub() {
    private val main by lazy { Shell.getShell() }

    private val fileManager by lazy {
        FileManager()
    }

    private val moduleManager by lazy {
        when (platform) {
            Platform.Magisk -> MagiskModuleManager(
                shell = main,
                seLinuxContext = seLinuxContext,
                fileManager = fileManager
            )

            Platform.KernelSU -> KernelSUModuleManager(
                shell = main,
                seLinuxContext = seLinuxContext,
                fileManager = fileManager
            )

            Platform.KsuNext -> KsuNextModuleManager(
                shell = main,
                seLinuxContext = seLinuxContext,
                fileManager = fileManager
            )

            Platform.APatch -> APatchModuleManager(
                shell = main,
                seLinuxContext = seLinuxContext,
                fileManager = fileManager
            )

            else -> throw IllegalStateException("Unsupported platform: $seLinuxContext")
        }
    }

    override fun getUid(): Int {
        return Os.getuid()
    }

    override fun getPid(): Int {
        return Os.getpid()
    }

    override fun getSELinuxContext(): String {
        return SELinux.getContext()
    }

    override fun currentPlatform(): String {
        return platform.name.lowercase()
    }

    override fun getModuleManager(): IModuleManager {
        return moduleManager
    }

    override fun getFileManager(): IFileManager {
        return fileManager
    }
}