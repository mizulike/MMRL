package com.dergoogler.mmrl.webui.client

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.UriHandler
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.PromptData
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.forbiddenResponse
import com.dergoogler.mmrl.webui.util.WebUIOptions

internal class WebUIClient(
    private val context: Context,
    private val options: WebUIOptions,
    private val uriHandler: UriHandler,
    private val debug: Boolean,
    private val webuiAssetsLoader: (Uri) -> WebResourceResponse?,
) : WebViewClient() {
    class ChromeClient(
        private val context: Context,
        private val showConfirm: (ConfirmData) -> Unit,
        private val showPrompt: (PromptData) -> Unit,
        private val options: WebUIOptions,
    ) : WebChromeClient() {
        override fun onJsAlert(
            view: WebView?,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            showConfirm(
                ConfirmData(
                    title = context.getString(R.string.says, options.modId.id),
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
                ConfirmData(
                    title = context.getString(R.string.says, options.modId.id),
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
                        options.modId.id
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

        return if (!options.isDomainSafe(mUrl)) {
            uriHandler.openUri(uri = mUrl)
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
        if (debug) {
            handler?.proceed()
        } else {
            handler?.cancel()
        }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? =  webuiAssetsLoader(request.url)

    private companion object {
        const val TAG = "WebUIClient"
    }
}
