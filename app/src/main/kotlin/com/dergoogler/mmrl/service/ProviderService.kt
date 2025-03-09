package com.dergoogler.mmrl.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.utils.NotificationUtils
import com.dergoogler.mmrl.datastore.model.WorkingMode
import kotlinx.coroutines.launch
import timber.log.Timber


class ProviderService : LifecycleService() {
    override fun onCreate() {
        Timber.d("onCreate")
        super.onCreate()
        isActive = true
        setForeground()
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isActive = false
        Timber.d("onDestroy")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            Timber.w("onStartCommand: intent is null")
            return START_NOT_STICKY
        }

        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(WORKING_MODE_KEY, WorkingMode::class.java)
                ?: WorkingMode.MODE_NON_ROOT
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(WORKING_MODE_KEY) as WorkingMode
        }

        lifecycleScope.launch {
            isActive = Compat.init(baseContext, mode)
        }

        return START_STICKY
    }

    private fun baseNotificationBuilder() =
        NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_PROVIDER)
            .setSmallIcon(R.drawable.launcher_outline)

    private fun setForeground() {
        val notification = baseNotificationBuilder()
            .setContentTitle("Provider Service is running")
            .setSilent(true)
            .setOngoing(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        startForeground(NotificationUtils.NOTIFICATION_ID_PROVIDER, notification)
    }

    companion object {
        var isActive by mutableStateOf(false)
            private set
        private const val GROUP_KEY = "PROVIDER_SERVICE_GROUP_KEY"
        private const val WORKING_MODE_KEY = "WORKING_MODE"

        fun start(
            context: Context,
            mode: WorkingMode,
        ) {
            val intent = Intent().apply {
                component = ComponentName(
                    context.packageName,
                    ProviderService::class.java.name
                )
                putExtra(WORKING_MODE_KEY, mode)
            }
            context.startService(intent)
        }

        fun stop(
            context: Context,
        ) {
            val intent = Intent(context, ProviderService::class.java)
            context.stopService(intent)
        }
    }
}