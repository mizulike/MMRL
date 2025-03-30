package com.dergoogler.mmrl.ui.activity.webui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.webUiConfig
import dev.dergoogler.mmrl.compat.BuildCompat
import dev.dergoogler.mmrl.compat.activity.MMRLComponentActivity
import dev.dergoogler.mmrl.compat.activity.setBaseContent
import timber.log.Timber

class WebUIActivity : MMRLComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("WebUIActivity onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!ProviderService.isActive) {
            setBaseContent {
                Failed(
                    message = stringResource(id = R.string.provider_service_not_active),
                )
            }

            return
        }

        val modId = intent.getStringExtra("MOD_ID")

        if (modId.isNullOrEmpty()) {
            setBaseContent {
                Failed(
                    message = stringResource(id = R.string.missing_mod_id),
                )
            }

            return
        }

        val config = webUiConfig(modId)

        if (config.title != null) {
            val taskDescription = if (BuildCompat.atLeastT) {
                ActivityManager.TaskDescription.Builder()
                    .setLabel(config.title)
                    .build()
            } else {
                ActivityManager.TaskDescription(
                    config.title
                )
            }

            setTaskDescription(taskDescription)
        }


        setBaseContent {
            val viewModel =
                hiltViewModel<WebUIViewModel, WebUIViewModel.Factory> { factory ->
                    factory.create(modId)
                }

            WebUIScreen(viewModel)
        }
    }

    override fun onDestroy() {
        Timber.d("WebUIActivity onDestroy")
        super.onDestroy()
    }

    companion object {
        fun start(context: Context, modId: String) {
            val intent = Intent(context, WebUIActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    putExtra("MOD_ID", modId)
                }

            context.startActivity(intent)
        }
    }
}