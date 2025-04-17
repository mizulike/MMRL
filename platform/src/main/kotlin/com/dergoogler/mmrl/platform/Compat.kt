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

object Compat {
    private var mServiceOrNull: IServiceManager? = null
    private val mService
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
        debug: Boolean = false,
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
                    -> serviceManager.fromLibSu(platform, debug)

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

    val moduleManager: IModuleManager get() = mService.moduleManager
    val fileManager: IFileManager get() = mService.fileManager

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
}