package com.dergoogler.mmrl.platform.service

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.SELinux
import android.system.Os
import android.util.Log
import com.dergoogler.mmrl.platform.BINDER_TRANSACTION
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.manager.APatchModuleManager
import com.dergoogler.mmrl.platform.manager.KernelSUModuleManager
import com.dergoogler.mmrl.platform.manager.KsuNextModuleManager
import com.dergoogler.mmrl.platform.manager.MagiskModuleManager
import com.dergoogler.mmrl.platform.manager.StubModuleManager
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager

class ServiceManager(
    private val platform: Platform,
) : IServiceManager.Stub() {
    private val services = hashMapOf<String, IBinder>()

    private val fileManager by lazy {
        FileManager()
    }

    private val moduleManager by lazy {
        when (platform) {
            Platform.Magisk -> MagiskModuleManager(
                fileManager = fileManager
            )

            Platform.KernelSU -> KernelSUModuleManager(
                fileManager = fileManager
            )

            Platform.KsuNext -> KsuNextModuleManager(
                fileManager = fileManager
            )

            Platform.APatch -> APatchModuleManager(
                fileManager = fileManager
            )

            Platform.NonRoot -> StubModuleManager(
                fileManager = fileManager
            )
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

    override fun addService(service: Service<*>): IBinder? =
        runCatching {
            service.create(this).apply {
                services[service.name] = this
            }

        }.onFailure {
            Log.e(TAG, Log.getStackTraceString(it))

        }.getOrNull()

    override fun getService(name: String): IBinder? = services[name]

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int) =
        if (code == BINDER_TRANSACTION) {
            data.enforceInterface(DESCRIPTOR)
            val targetBinder = data.readStrongBinder()
            val targetCode = data.readInt()
            val targetFlags = data.readInt()
            val newData = Parcel.obtain()

            try {
                newData.appendFrom(data, data.dataPosition(), data.dataAvail())

                val id = Binder.clearCallingIdentity()
                targetBinder.transact(targetCode, newData, reply, targetFlags)
                Binder.restoreCallingIdentity(id)
            } finally {
                newData.recycle()
            }

            true
        } else {
            super.onTransact(code, data, reply, flags)
        }

    private companion object Default {
        const val TAG = "MMRLServiceManager"
    }
}
