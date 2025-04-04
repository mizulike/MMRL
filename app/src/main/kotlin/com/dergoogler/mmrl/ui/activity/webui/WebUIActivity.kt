package com.dergoogler.mmrl.ui.activity.webui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.webUiConfig
import dev.dergoogler.mmrl.compat.BuildCompat
import dev.dergoogler.mmrl.compat.activity.MMRLComponentActivity
import dev.dergoogler.mmrl.compat.activity.setBaseContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber


class WebUIActivity : MMRLComponentActivity() {

    private val userPrefs get() = runBlocking { userPreferencesRepository.data.first() }
    private lateinit var webView: WebView
    private var isKeyboardShowing by mutableStateOf(false)
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("WebUIActivity onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        webView = WebView(this)
        rootView = findViewById(android.R.id.content)

        lifecycleScope.launch {
            Compat.init(this@WebUIActivity, userPrefs.workingMode)
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

        setBaseContent {

            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Compat.isAlive) {
                while (!Compat.isAlive) {
                    delay(1000)
                }

                isLoading = false
            }

            if (isLoading) {
                Loading()

                return@setBaseContent
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

            if (config.windowResize) {
                rootView.getViewTreeObserver().addOnGlobalLayoutListener {
                    val r: Rect = Rect()
                    rootView.getWindowVisibleDisplayFrame(r)
                    val screenHeight: Int = rootView.getRootView().height
                    val keypadHeight: Int = screenHeight - r.bottom
                    if (keypadHeight > screenHeight * 0.15) {
                        if (!isKeyboardShowing) {
                            isKeyboardShowing = true
                            adjustWebViewHeight(keypadHeight)
                        }
                    } else {
                        if (isKeyboardShowing) {
                            isKeyboardShowing = false
                            resetWebViewHeight()
                        }
                    }
                }
            }

            val viewModel =
                hiltViewModel<WebUIViewModel, WebUIViewModel.Factory> { factory ->
                    factory.create(modId)
                }

            WebUIScreen(webView, viewModel)
        }
    }

    private fun adjustWebViewHeight(keypadHeight: Int) {
        val params = webView.layoutParams
        params.height = rootView.height - keypadHeight
        webView.layoutParams = params
    }

    private fun resetWebViewHeight() {
        val params = webView.layoutParams
        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        webView.layoutParams = params
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
