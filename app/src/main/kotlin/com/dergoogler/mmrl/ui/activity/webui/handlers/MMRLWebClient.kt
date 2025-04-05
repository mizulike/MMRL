package com.dergoogler.mmrl.ui.activity.webui.handlers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.net.toUri
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.PromptData
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import dev.dergoogler.mmrl.compat.core.MMRLUriHandlerImpl


class MMRLWebClient(
    private val context: Context,
    private val viewModel: WebUIViewModel,
    private val browser: MMRLUriHandlerImpl,
    private val userPrefs: UserPreferences,
    private val webuiAssetsLoader: (Uri) -> WebResourceResponse?,
) : WebViewClient() {
    class MMRLChromeClient(
        private val context: Context,
        private val showConfirm: (ConfirmData) -> Unit,
        private val showPrompt: (PromptData) -> Unit,
        private val viewModel: WebUIViewModel,
    ) : WebChromeClient() {
        override fun onJsAlert(
            view: WebView?,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            showConfirm(
                ConfirmData(title = context.getString(R.string.says, viewModel.modId),
                    description = message,
                    onConfirm = { result.confirm() },
                    onClose = { result.cancel() })
            )

            return true
        }

        override fun onJsConfirm(
            view: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            showConfirm(
                ConfirmData(title = context.getString(R.string.says, viewModel.modId),
                    description = message,
                    onConfirm = {
                        result.confirm()
                    },
                    onClose = {
                        result.cancel()
                    })
            )

            return true
        }

        override fun onJsPrompt(
            view: WebView,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult,
        ): Boolean {
            showPrompt(
                PromptData(
                    title = message ?: context.getString(
                        R.string.says,
                        viewModel.modId
                    ),
                    value = defaultValue ?: "",
                    onConfirm = {
                        result.confirm(it)
                    },
                    onClose = {
                        result.cancel()
                    }
                )
            )

            return true
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest?,
    ): Boolean {
        val mUrl = request?.url?.toString() ?: return false

        return if (!viewModel.isDomainSafe(mUrl)) {
            browser.openUri(uri = mUrl, onSuccess = { intent, uri ->
                intent.launchUrl(context, uri.toUri())
                Toast.makeText(
                    context, context.getString(
                        R.string.unsafe_url_redirecting, uri
                    ), Toast.LENGTH_SHORT
                ).show()
            })
            true
        } else {
            view.loadUrl(mUrl)
            false
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?,
    ) {
        if (userPrefs.developerMode && userPrefs.useWebUiDevUrl) {
            handler?.proceed()
        } else {
            handler?.cancel()
        }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return webuiAssetsLoader(request.url)
    }
}