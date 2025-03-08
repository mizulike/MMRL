package com.dergoogler.mmrl.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.utils.NotificationUtils
import com.dergoogler.mmrl.database.entity.Repo.Companion.toRepo
import dev.dergoogler.mmrl.compat.worker.MMRLLifecycleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.hours

class RepositoryService : MMRLLifecycleService() {
    override val title = R.string.notification_name_repository
    override val notificationId = NotificationUtils.NOTIFICATION_ID_REPOSITORY
    override val channelId = NotificationUtils.CHANNEL_ID_REPOSITORY
    override val groupKey = "REPOSITORY_SERVICE_GROUP_KEY"

    override fun onCreate() {
        super.onCreate()
        isActive.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive.value = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            Timber.w("onStartCommand: intent is null")
            return START_NOT_STICKY
        }

        val interval = intent.getLongExtra(INTERVAL_KEY, 60000)

        lifecycleScope.launch {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    try {
                        val repoDao = database.repoDao()
                        val repos = repoDao.getAll()

                        repos.forEach { repo ->
                            try {
                                modulesRepository.getRepo(repo.url.toRepo())
                                    .onSuccess {
                                        sendSuccessNotification()
                                    }.onFailure {
                                        sendFailureNotification()
                                    }
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }

                    } catch (e: Exception) {
                        sendFailureNotification()
                    }

                    delay(interval.hours)
                }
            }
        }

        return START_STICKY
    }

    private fun sendSuccessNotification() {
        pushNotification(
            icon = R.drawable.cloud,
            title = applicationContext.getString(R.string.repo_update_service),
            message = applicationContext.getString(R.string.repo_update_service_desc)
        )
    }

    private fun sendFailureNotification() {
        pushNotification(
            icon = R.drawable.cloud,
            title = applicationContext.getString(R.string.repo_update_service_failed),
            message = applicationContext.getString(R.string.repo_update_service_failed_desc)
        )
    }

    companion object {
        val isActive = MutableStateFlow(false)
        private const val INTERVAL_KEY = "INTERVAL"

        fun start(
            context: Context,
            interval: Long,
        ) {
            val intent = Intent().apply {
                component = ComponentName(
                    context.packageName,
                    RepositoryService::class.java.name
                )
                putExtra(INTERVAL_KEY, interval)
            }
            context.startService(intent)

        }


        fun stop(
            context: Context,
        ) {
            val intent = Intent(context, RepositoryService::class.java)
            context.stopService(intent)
        }
    }
}