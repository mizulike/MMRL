package com.dergoogler.mmrl.webui.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.webui.interfaces.FileInterface
import com.dergoogler.mmrl.webui.LocalInsets
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.client.WebUIClient
import com.dergoogler.mmrl.webui.component.Loading
import com.dergoogler.mmrl.webui.component.dialog.ConfirmData
import com.dergoogler.mmrl.webui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.webui.component.dialog.rememberPrompt
import com.dergoogler.mmrl.webui.handler.mmrlPathHandler
import com.dergoogler.mmrl.webui.handler.suPathHandler
import com.dergoogler.mmrl.webui.interfaces.FileInputInterface
import com.dergoogler.mmrl.webui.interfaces.ModuleInterface
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.rememberInsets
import com.dergoogler.mmrl.webui.rememberWebUIAssetLoader
import com.dergoogler.mmrl.webui.viewModel.WebUIViewModel
import com.dergoogler.webui.handlers.webrootPathHandler
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.i
import kotlinx.html.lang
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.ul


@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebUIScreen(
    webView: WebView,
    viewModel: WebUIViewModel,
    interfaces: List<JavaScriptInterface> = listOf(),
) {
    val context = LocalContext.current
    val showConfirm = rememberConfirm(context)
    val showPrompt = rememberPrompt(context)
    val uriHandler = LocalUriHandler.current
    val colorScheme = MaterialTheme.colorScheme

    WebView.setWebContentsDebuggingEnabled(viewModel.debug)

    BackHandler {
        if (viewModel.config.backHandler && webView.canGoBack()) {
            webView.goBack()
            return@BackHandler
        }

        if (viewModel.config.exitConfirm) {
            showConfirm(ConfirmData(
                title = context.getString(R.string.exit),
                description = context.getString(R.string.exit_desc),
                onConfirm = { (context as Activity).finish() },
                onClose = {}
            ))
            return@BackHandler
        }

        (context as Activity).finish()
    }

    val insets = rememberInsets()

    if (insets != null) {
        CompositionLocalProvider(
            LocalInsets provides insets
        ) {
            val webuiAssetsLoader = rememberWebUIAssetLoader(
                handlers = listOf(
                    "/mmrl/" to mmrlPathHandler(viewModel),
                    ".${viewModel.modId}/" to suPathHandler("/data/adb/modules/${viewModel.modId}".toSuFile()),
                    "/.adb/" to suPathHandler("/data/adb".toSuFile()),
                    "/.config/" to suPathHandler("/data/adb/.config".toSuFile()),
                    "/.local/" to suPathHandler("/data/adb/.local".toSuFile()),
                    "/" to webrootPathHandler(viewModel),
                )
            )

            key(viewModel.recomposeCount) {
                AndroidView(factory = {
                    webView.apply {
                        setBackgroundColor(colorScheme.background.toArgb())
                        background = ColorDrawable(colorScheme.background.toArgb())
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        ViewCompat.setOnApplyWindowInsetsListener(this) { _, o ->
                            o.inset(insets.top, insets.bottom, insets.left, insets.right)
                        }

                        webViewClient = WebUIClient(
                            context = context,
                            uriHandler = uriHandler,
                            webuiAssetsLoader = webuiAssetsLoader,
                            debug = viewModel.debug,
                            viewModel = viewModel,
                        )

                        webChromeClient = WebUIClient.ChromeClient(
                            context = context,
                            viewModel = viewModel,
                            showPrompt = showPrompt,
                            showConfirm = showConfirm,
                        )

                        for (i in interfaces) {
                            addJavascriptInterface(
                                i.instance, i.name
                            )
                        }

                        addJavascriptInterface(
                            FileInputInterface(
                                context = context,
                                webView = this,
                            ), viewModel.sanitizedModIdWithFileInputStream
                        )

                        addJavascriptInterface(
                            ModuleInterface(
                                viewModel = viewModel,
                                context = context,
                                webView = this,
                                insets = insets
                            ), "$${viewModel.sanitizedModId}"
                        )

                        addJavascriptInterface(
                            FileInterface(this, context), viewModel.sanitizedModIdWithFile
                        )
                    }
                }, update = {
                    it.apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            if (viewModel.debug && viewModel.remoteDebug) {
                                mixedContentMode =
                                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            }
                            userAgentString = "DON'T TRACK ME DOWN MOTHERFUCKER!"
                        }


                        if (viewModel.requireNewAppVersion) {
                            loadData(
                                getRequireNewVersion(context, viewModel), "text/html", "UTF-8"
                            )

                            return@apply
                        }

                        loadUrl(viewModel.domainUrl)
                    }
                })
            }
        }
    } else {
        Loading()
    }
}

fun getRequireNewVersion(
    context: Context,
    viewModel: WebUIViewModel,
) = buildString {
    appendHTML().html {
        lang = "en"
        head {
            meta {
                name = "viewport"
                content =
                    "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0"
            }
            link {
                rel = "stylesheet"
                href = "${viewModel.domain}/mmrl/insets.css"
            }
            link {
                rel = "stylesheet"
                href = "${viewModel.domain}/mmrl/colors.css"
            }
            link {
                rel = "stylesheet"
                href = "${viewModel.domain}/mmrl/assets/webui/requireNewVersion.css"
            }
            title { +"New App Version Required" }
        }
        body {
            div(classes = "container") {
                div(classes = "content") {
                    div(classes = "title") { +context.getString(R.string.requireNewVersion_cannot_load_webui) }
                    div {
                        b { +viewModel.modId }
                        +" "
                        +context.getString(R.string.requireNewVersion_require_text)
                        +" "
                        i { +viewModel.config.require.version.required.toString() }
                    }
                    div(classes = "list") {
                        span { +context.getString(R.string.requireNewVersion_try_the_following) }
                        ul {
                            li { +context.getString(R.string.requireNewVersion_try_the_following_one) }
                            li { +context.getString(R.string.requireNewVersion_try_the_following_two) }
                        }
                    }
                    div(classes = "code") { +"ERR_NEW_MMRL_REQUIRED" }
                    div(classes = "buttons") {
                        button(classes = "refresh") {
                            onClick = "location.reload();"
                            +context.getString(R.string.requireNewVersion_refresh)
                        }

                        val supportLink = viewModel.config.require.version.supportLink
                        val supportText = viewModel.config.require.version.supportText

                        if (supportLink != null && supportText != null) {
                            button(classes = "more") {
                                attributes["onclick"] = "window.open('$supportLink');"
                                +supportText
                            }
                        }
                    }
                }
            }
        }
    }
}