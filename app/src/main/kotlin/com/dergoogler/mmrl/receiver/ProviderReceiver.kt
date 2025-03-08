package com.dergoogler.mmrl.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import com.dergoogler.mmrl.service.ProviderService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProviderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val preferences = runBlocking { userPreferencesRepository.data.first() }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                runBlocking {
                    Timber.d("Boot completed. Starting provider service.")
                    ProviderService.start(
                        context = context,
                        preferences = preferences
                    )
                }
            }
        }
    }
}