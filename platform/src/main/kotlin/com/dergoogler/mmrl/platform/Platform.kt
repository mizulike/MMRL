package com.dergoogler.mmrl.platform

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
import com.dergoogler.mmrl.platform.model.PlatformConfig
import com.dergoogler.mmrl.platform.model.PlatformConfigImpl
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.FileDescriptor

const val TIMEOUT_MILLIS = 15_000L
const val PLATFORM_KEY = "PLATFORM"
internal const val BINDER_TRANSACTION = 84398154

/**
 * Represents the various platforms supported by the application.
 *
 * @property id A unique identifier for the platform.
 */
enum class Platform(val id: String) {
    Magisk("magisk"),
    KernelSU("kernelsu"),
    KsuNext("ksunext"),
    APatch("apatch"),
    NonRoot("");

    companion object {
        private const val TAG = "Platform"
        var mServiceOrNull: IServiceManager? = null
        val mService
            get() = checkNotNull(mServiceOrNull) {
                "IServiceManager haven't been received"
            }

        var isAlive by mutableStateOf(false)
            private set

        private val _isAliveFlow = MutableStateFlow(false)
        val isAliveFlow get() = _isAliveFlow.asStateFlow()

        /**
         * Initializes the platform with the given configuration.
         *
         * This function sets up the underlying service manager for the platform. It requires
         * a valid `android.content.Context` and a supported `com.dergoogler.mmrl.platform.Platform`
         * to function correctly. If the platform is already initialized, it simply returns `true`.
         *
         * @param config A lambda function with a receiver of type `PlatformConfig` that allows
         *               configuring the platform's settings (e.g., context, platform type, debug mode).
         *               This lambda is executed within the scope of a `PlatformConfig` instance,
         *               allowing direct access to its properties and functions.
         * @return `true` if the platform was successfully initialized or was already initialized,
         *         `false` if the initialization failed due to missing configuration or an error.
         *
         * @throws Exception If any unexpected error occurs during the initialization process.
         *
         * **Error Handling:**
         * - If the provided configuration doesn't include a `context`, it logs an error and returns `false`.
         * - If the configuration lacks a specified `platform` type, it also logs an error and returns `false`.
         * - If an exception occurs during service manager creation, it catches the exception, logs it, clears the service, and returns the current `state()`
         *
         * **Initialization Process:**
         * 1. Applies the provided configuration lambda to a `PlatformConfigImpl` instance.
         * 2. Checks if the context and platform are defined in the configuration.
         * 3. If the platform is already alive (`isAlive` is true), returns `true` immediately.
         * 4. Otherwise, attempts to create the service manager based on the specified platform.
         * 5. Uses `ServiceManagerCompat.fromLibSu` for Magisk, KsuNext, KernelSU, and APatch platforms, if no `fromProvider` is specified.
         * 6. If no `fromProvider` and a different platform, it sets the service to null.
         * 7. Calls the `state()` function to get the initialized or current state.
         * 8. Returns true if no exception occurs and state() is successful
         * 9. On exception clears the service to null and returns the state()
         */
        suspend fun init(config: suspend PlatformConfig.() -> Unit): Boolean {
            val conf = PlatformConfigImpl().applyConfig(config)

            if (conf.context == null) {
                throw IllegalArgumentException("Context cannot be null")
            }

            if (conf.platform == null) {
                throw IllegalArgumentException("Platform cannot be null")
            }

            if (conf.provider == null) {
                throw IllegalArgumentException("Provider cannot be null")
            }

            return when {
                isAlive -> true
                else -> try {
                    mServiceOrNull = when (conf.platform) {
                        Magisk,
                        KsuNext,
                        KernelSU,
                        APatch,
                            -> conf.provider

                        else -> null
                    }

                    state()
                } catch (e: Exception) {
                    mServiceOrNull = null
                    Log.e(TAG, "Failed to init service manager", e)
                    state()
                }
            }
        }

        suspend fun PlatformConfigImpl.applyConfig(block: suspend PlatformConfig.() -> Unit): PlatformConfigImpl {
            block()
            return this
        }

        /**
         * Provides access to the module manager, which is responsible for managing the available modules.
         *
         * The module manager allows you to:
         * - Get a list of all available modules.
         * - Find a specific module by its identifier.
         * - Check if a module is enabled or disabled.
         * - (Potentially) Enable or disable modules, depending on the underlying implementation.
         * - (Potentially) Interact with individual modules.
         *
         * This property delegates to the module manager held by the underlying service [mService].
         *
         * @see IModuleManager
         */
        val moduleManager: IModuleManager get() = mService.moduleManager

        /**
         * Provides access to the file management service.
         *
         * This property allows interaction with the underlying file management system, enabling
         * operations like creating, deleting, reading, and writing files and directories.
         *
         * @see IFileManager
         */
        val fileManager: IFileManager get() = mService.fileManager

        /**
         * The SELinux context associated with the binder service.
         *
         * This property provides the SELinux security context string that the binder service is running under.
         * This context is used by the kernel's SELinux module to enforce security policies and control access to resources.
         *
         * @see [android.os.Binder.getCallingPid] For more information about how process SELinux contexts are managed.
         * @see [android.os.Binder.getCallingUid] For more information about how user SELinux contexts are managed.
         * @see [android.os.Binder] For more information about the Binder IPC mechanism.
         *
         * @return A String representing the SELinux context of the binder service.
         */
        val seLinuxContext: String get() = mService.seLinuxContext

        val platform: Platform
            get() = if (mServiceOrNull != null) Platform.from(mService.currentPlatform()) else NonRoot

        private fun state(): Boolean {
            isAlive = mServiceOrNull != null
            _isAliveFlow.value = isAlive

            return isAlive
        }

        fun <T> get(fallback: T, block: Companion.() -> T): T {
            return when {
                isAlive -> block(this)
                else -> fallback
            }
        }

        fun setHiddenApiExemptions(vararg signaturePrefixes: String = arrayOf("")) = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> HiddenApiBypass.addHiddenApiExemptions(
                *signaturePrefixes
            )

            else -> true
        }

        fun from(value: String): Platform {
            return entries.firstOrNull { it.id == value } ?: NonRoot
        }
    }

    val isMagisk get() = this == Magisk
    val isKernelSU get() = this == KernelSU
    val isKernelSuNext get() = this == KsuNext
    val isAPatch get() = this == APatch

    val isNotMagisk get() = !isMagisk
    val isNotKernelSU get() = this != KernelSU && this != KsuNext
    val isNotKernelSuNext get() = !isKernelSuNext
    val isNotAPatch get() = !isAPatch

    val isValid get() = this != NonRoot
    val isKernelSuOrNext get() = this == KernelSU || this == KsuNext

    val current get() = id
    
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
