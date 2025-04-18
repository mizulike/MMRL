package com.dergoogler.mmrl.platform

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.service.ServiceManagerCompat
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass

enum class Platform(val id: String) {
    Magisk("magisk"),
    KernelSU("kernelsu"),
    KsuNext("ksunext"),
    APatch("apatch"),
    NonRoot("");

    companion object {
        private const val TAG = "Platform"
        private var mServiceOrNull: IServiceManager? = null
        private val mService
            get() = checkNotNull(mServiceOrNull) {
                "IServiceManager haven't been received"
            }

        var isAlive by mutableStateOf(false)
            private set

        private val _isAliveFlow = MutableStateFlow(false)
        val isAliveFlow get() = _isAliveFlow.asStateFlow()

        /**
         * Initializes the platform with the specified configuration.
         *
         * This function is a suspending function, meaning it can be called from a coroutine
         * or another suspending function. It allows you to configure the platform by providing
         * a lambda that applies settings to a [PlatformConfig] instance.
         *
         * **Note:** The properties [PlatformConfig.context] and [PlatformConfig.platform]
         * must be set in the configuration. If they are not provided, this function will return `false`.
         *
         * @param config A lambda function used to configure the platform. The lambda receives
         *               a [PlatformConfig] instance as its receiver, allowing you to set
         *               various configuration options.
         * @return `true` if the initialization was successful, `false` otherwise.
         */
        suspend fun init(config: suspend PlatformConfig.() -> Unit): Boolean {
            val conf = PlatformConfigImpl().applyConfig(config)

            if (conf.context == null) {
                Log.e(TAG, "Cannot initialize Platform without defining 'android.content.Context'")
                return false
            }

            if (conf.platform == null) {
                Log.e(
                    TAG,
                    "Cannot initialize Platform without defining 'com.dergoogler.mmrl.platform.Platform'"
                )
                return false
            }

            return when {
                isAlive -> true
                else -> try {
                    mServiceOrNull = when (conf.platform) {
                        Magisk,
                        KsuNext,
                        KernelSU,
                        APatch,
                        -> conf.fromProvider
                            ?: ServiceManagerCompat.fromLibSu(
                                context = conf.context!!,
                                platform = conf.platform!!,
                                debug = conf.debug
                            )

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

        val moduleManager: IModuleManager get() = mService.moduleManager
        val fileManager: IFileManager get() = mService.fileManager

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
}

interface PlatformConfig {
    var context: Context?
    var platform: Platform?
    var debug: Boolean
    var fromProvider: IServiceManager?
}

data class PlatformConfigImpl(
    override var context: Context? = null,
    override var platform: Platform? = null,
    override var debug: Boolean = false,
    override var fromProvider: IServiceManager? = null,
) : PlatformConfig