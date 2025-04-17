package com.dergoogler.mmrl.platform.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import com.dergoogler.mmrl.platform.Compat.addService
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.content.IService
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.manager.KernelSUModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceManagerCompat(
    private val context: Context,
) {
    interface IProvider {
        val name: String
        fun isAvailable(): Boolean
        suspend fun isAuthorized(): Boolean
        fun bind(connection: ServiceConnection)
        fun unbind(connection: ServiceConnection)
    }

    private suspend fun get(
        provider: IProvider
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

    suspend fun from(provider: IProvider): IServiceManager =
        withContext(Dispatchers.Main) {
            when {
                !provider.isAvailable() -> throw IllegalStateException("${provider.name} not available")
                !provider.isAuthorized() -> throw IllegalStateException("${provider.name} not authorized")
                else -> get(provider)
            }
        }

    private class SuService : RootService() {
        override fun onBind(intent: Intent): IBinder {
            val platform = intent.getPlatform()
            val payload = intent.getServicePayload()

            return ServiceManager(platform).apply {
                payload.services.forEach { addService(it) }
            }
        }

    }

    private class LibSuProvider(
        private val context: Context,
        private val platform: Platform,
        private val payload: ServicePayload,
    ) : IProvider {
        override val name = "LibSu"

        init {
            Shell.enableVerboseLogging = true
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
            RootService.bind(getWorkingModeIntent(context, platform, payload), connection)
        }

        override fun unbind(connection: ServiceConnection) {
            RootService.stop(getWorkingModeIntent(context, platform, payload))
        }

        private class SuShellInitializer : Shell.Initializer() {
            override fun onInit(context: Context, shell: Shell) = shell.isRoot
        }
    }

    suspend fun fromLibSu(
        platform: Platform,
        services: List<Class<out IService>>,
    ) =
        from(
            LibSuProvider(
                context = context,
                platform = platform,
                payload = ServicePayload(
                    services = services.map { Service(it) },
                )
            )
        )

    companion object {
        internal const val VERSION_CODE = 1
        private const val TIMEOUT_MILLIS = 15_000L
        internal const val BINDER_TRANSACTION = 84398154

        const val PLATFORM_KEY = "PLATFORM"
        const val SERVICES_KEY = "SERVICES"

        @Parcelize
        data class ServicePayload(
            val services: List<Service<out IService>>,
        ) : Parcelable {
            companion object {
                val EMPTY = ServicePayload(emptyList())
            }
        }

        fun Intent.getPlatform(): Platform =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getSerializableExtra(PLATFORM_KEY, Platform::class.java)
                    ?: Platform.NonRoot
            } else {
                @Suppress("DEPRECATION")
                this.getSerializableExtra(PLATFORM_KEY) as Platform
            }

        fun Intent.getServicePayload(): ServicePayload =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableExtra(PLATFORM_KEY, ServicePayload::class.java)
            } else {
                @Suppress("DEPRECATION")
                this.getParcelableExtra(PLATFORM_KEY) ?: ServicePayload.EMPTY
            } ?: ServicePayload.EMPTY

        private fun getWorkingModeIntent(
            context: Context,
            platform: Platform,
            servicePayload: ServicePayload,
        ) = Intent().apply {
            component = ComponentName(
                context.packageName,
                SuService::class.java.name
            )
            putExtra(PLATFORM_KEY, platform)
            putExtra(SERVICES_KEY, servicePayload)
        }
    }
}