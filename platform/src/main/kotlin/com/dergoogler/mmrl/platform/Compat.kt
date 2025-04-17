package com.dergoogler.mmrl.platform

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.ServiceManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.content.IService
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.service.ServiceManagerCompat
import com.dergoogler.mmrl.platform.service.ServiceManagerCompat.Companion.BINDER_TRANSACTION
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.FileDescriptor

object Compat {
    private var mServiceOrNull: IServiceManager? = null
    val mService
        get() = checkNotNull(mServiceOrNull) {
            "IServiceManager haven't been received"
        }

    var isAlive by mutableStateOf(false)
        private set

    private val _isAliveFlow = MutableStateFlow(false)
    val isAliveFlow get() = _isAliveFlow.asStateFlow()

    suspend fun init(
        context: Context,
        platform: Platform,
        services: List<Class<out IService>> = emptyList(),
    ): Boolean {
        val serviceManager = ServiceManagerCompat(context)

        return when {
            isAlive -> true
            else -> try {
                mServiceOrNull = when (platform) {
                    Platform.Magisk,
                    Platform.KsuNext,
                    Platform.KernelSU,
                    Platform.APatch,
                    -> serviceManager.fromLibSu(platform, services)

                    else -> null
                }

                state()
            } catch (e: Exception) {
                mServiceOrNull = null
                Log.e("Compat", "Failed to init service manager", e)
                state()
            }
        }
    }

    val platform: Platform
        get() = if (mServiceOrNull != null) Platform.from(mService.currentPlatform()) else Platform.NonRoot

    private fun state(): Boolean {
        isAlive = mServiceOrNull != null
        _isAliveFlow.value = isAlive

        return isAlive
    }

    fun <T> get(fallback: T, block: Compat.() -> T): T {
        return when {
            isAlive -> block(this)
            else -> fallback
        }
    }

    inline fun <T> withNewRootShell(
        globalMnt: Boolean = false,
        debug: Boolean = false,
        commands: Array<String> = arrayOf("su"),
        block: Shell.() -> T,
    ): T {
        return createRootShell(globalMnt, debug, commands).use(block)
    }

    fun createRootShell(
        globalMnt: Boolean = false,
        debug: Boolean = false,
        commands: Array<String> = arrayOf("su"),
    ): Shell {
        Shell.enableVerboseLogging = debug
        val builder = Shell.Builder.create()
        if (globalMnt) {
            builder.setFlags(Shell.FLAG_MOUNT_MASTER)
        }
        return builder.build(*commands)
    }

    fun setHiddenApiExemptions(vararg signaturePrefixes: String = arrayOf("")) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> HiddenApiBypass.addHiddenApiExemptions(*signaturePrefixes)
        else -> true
    }

    fun <T : IBinder> T.proxyBy(service: IServiceManager) = object : IBinder {
        override fun getInterfaceDescriptor() = this@proxyBy.interfaceDescriptor

        override fun pingBinder() = this@proxyBy.pingBinder()

        override fun isBinderAlive() = this@proxyBy.isBinderAlive

        override fun queryLocalInterface(descriptor: String) = null

        override fun dump(fd: FileDescriptor, args: Array<out String>?) =
            this@proxyBy.dump(fd, args)

        override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) =
            this@proxyBy.dumpAsync(fd, args)

        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) =
            this@proxyBy.linkToDeath(recipient, flags)

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int) =
            this@proxyBy.unlinkToDeath(recipient, flags)

        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val serviceBinder = service.asBinder()
            val newData = Parcel.obtain()

            try {
                newData.apply {
                    writeInterfaceToken(IServiceManager.DESCRIPTOR)
                    writeStrongBinder(this@proxyBy)
                    writeInt(code)
                    writeInt(flags)
                    appendFrom(data, 0, data.dataSize())
                }

                serviceBinder.transact(BINDER_TRANSACTION, newData, reply, 0)
            } finally {
                newData.recycle()
            }

            return true
        }
    }

    fun <T : IInterface> T.proxyBy(service: IServiceManager) =
        asBinder().proxyBy(service)

    fun <T : IServiceManager> T.getSystemService(name: String) =
        ServiceManager.getService(name).proxyBy(this)

    fun <T : IServiceManager, S : IService> T.addService(cls: Class<S>): IBinder? =
        addService(Service(cls))
}


val Compat.moduleManager: IModuleManager
    get() = mService.moduleManager

val Compat.fileManager: IFileManager get() = mService.fileManager