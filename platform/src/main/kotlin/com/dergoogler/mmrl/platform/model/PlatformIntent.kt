package com.dergoogler.mmrl.platform.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import com.dergoogler.mmrl.platform.PLATFORM_KEY
import com.dergoogler.mmrl.platform.Platform

interface IProvider {
    val name: String
    fun isAvailable(): Boolean
    suspend fun isAuthorized(): Boolean
    fun bind(connection: ServiceConnection)
    fun unbind(connection: ServiceConnection)
}

/**
 * Creates an [Intent] for a specific platform.
 *
 * This function is an inline extension function on the [Context] class.
 * It takes a reified type parameter `T` which represents the target component (e.g., Activity or Service)
 * and a [Platform] enum value indicating the platform for which the intent is being created.
 *
 * The created [Intent] will have its component set to the fully qualified name of the class `T`
 * within the current application's package.
 * It will also include the specified [platform] as an extra, using [PLATFORM_KEY] as the key.
 *
 * @param T The reified type of the target component (e.g., an Activity or Service class).
 * @param platform The [Platform] for which this intent is being created.
 * @return An [Intent] configured to launch the specified component for the given platform.
 */
inline fun <reified T> Context.createPlatformIntent(platform: Platform): Intent = Intent().apply {
    component = ComponentName(
        packageName,
        T::class.java.name
    )
    putExtra(PLATFORM_KEY, platform)
}

@Deprecated("Use Context.createPlatformIntent(...) instead")
data class PlatformIntent(
    val context: Context,
    val platform: Platform,
    val clazz: Class<*>,
) {
    companion object {
        fun Intent.getPlatform(): Platform =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getSerializableExtra(PLATFORM_KEY, Platform::class.java)
                    ?: Platform.NonRoot
            } else {
                @Suppress("DEPRECATION")
                this.getSerializableExtra(PLATFORM_KEY) as Platform
            }
    }

    val intent: Intent
        get() = Intent().apply {
            component = ComponentName(
                context.packageName,
                clazz.name
            )
            putExtra(PLATFORM_KEY, platform)
        }

}