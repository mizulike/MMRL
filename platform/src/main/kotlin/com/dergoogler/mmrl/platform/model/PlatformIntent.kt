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

data class PlatformIntent(
    val context: Context,
    val platform: Platform,
    val clazz: Class<*>
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