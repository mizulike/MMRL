package com.dergoogler.mmrl.platform.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.lsposed.hiddenapibypass.HiddenApiBypass
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface IProvider {
    val name: String
    fun isAvailable(): Boolean
    suspend fun isAuthorized(): Boolean
    fun bind(connection: ServiceConnection)
    fun unbind(connection: ServiceConnection)
}

object ServiceManagerCompat {
    const val TIMEOUT_MILLIS = 15_000L
    const val PLATFORM_KEY = "PLATFORM"

    suspend fun get(
        provider: IProvider,
    ) = withTimeout(TIMEOUT_MILLIS) {
        suspendCancellableCoroutine { continuation ->
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = IServiceManager.Stub.asInterface(binder)
                    continuation.resume(service)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    continuation.resumeWithException(
                        IllegalStateException("IServiceManager destroyed")
                    )
                }

                override fun onBindingDied(name: ComponentName?) {
                    continuation.resumeWithException(
                        IllegalStateException("IServiceManager destroyed")
                    )
                }
            }

            provider.bind(connection)
            continuation.invokeOnCancellation {
                provider.unbind(connection)
            }
        }
    }

    suspend fun from(provider: IProvider): IServiceManager = withContext(Dispatchers.Main) {
        when {
            !provider.isAvailable() -> throw IllegalStateException("${provider.name} not available")
            !provider.isAuthorized() -> throw IllegalStateException("${provider.name} not authorized")
            else -> get(provider)
        }
    }

    class SuService : RootService() {
        override fun onBind(intent: Intent): IBinder {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(PLATFORM_KEY, Platform::class.java)
                    ?: Platform.NonRoot
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(PLATFORM_KEY) as Platform
            }

            return ServiceManager(mode)
        }

    }

    class LibSuProvider(
        private val context: Context,
        private val platform: Platform,
        debug: Boolean,
    ) : IProvider {
        override val name = "LibSu"

        init {
            Shell.enableVerboseLogging = debug
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setInitializers(SuShellInitializer::class.java)
                    .setTimeout(10)
            )
        }

        override fun isAvailable() = true

        override suspend fun isAuthorized() = suspendCancellableCoroutine { continuation ->
            Shell.EXECUTOR.submit {
                runCatching {
                    Shell.getShell()
                }.onSuccess {
                    continuation.resume(true)
                }.onFailure {
                    continuation.resume(false)
                }
            }
        }

        override fun bind(connection: ServiceConnection) {
            RootService.bind(getPlatformIntent(context, platform), connection)
        }

        override fun unbind(connection: ServiceConnection) {
            RootService.stop(getPlatformIntent(context, platform))
        }
    }

    class SuShellInitializer : Shell.Initializer() {
        override fun onInit(context: Context, shell: Shell) = shell.isRoot
    }

    suspend fun fromLibSu(
        context: Context,
        platform: Platform,
        debug: Boolean,
    ) = from(LibSuProvider(context, platform, debug))

    fun Intent.getPlatform(): Platform =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSerializableExtra(PLATFORM_KEY, Platform::class.java)
                ?: Platform.NonRoot
        } else {
            @Suppress("DEPRECATION")
            this.getSerializableExtra(PLATFORM_KEY) as Platform
        }

    fun getPlatformIntent(context: Context, platform: Platform) = Intent().apply {
        component = ComponentName(
            context.packageName,
            SuService::class.java.name
        )
        putExtra(PLATFORM_KEY, platform)
    }

}
