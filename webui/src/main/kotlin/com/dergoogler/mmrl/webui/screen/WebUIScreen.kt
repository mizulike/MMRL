package com.dergoogler.mmrl.webui.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.ui.component.dialog.rememberPrompt
import com.dergoogler.mmrl.webui.handler.internalPathHandler
import com.dergoogler.mmrl.webui.handler.suPathHandler
import com.dergoogler.mmrl.webui.handler.webrootPathHandler
import com.dergoogler.mmrl.webui.interfaces.FileInputInterface
import com.dergoogler.mmrl.webui.interfaces.ModuleInterface
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.rememberInsets
import com.dergoogler.mmrl.webui.rememberWebUIAssetLoader
import com.dergoogler.mmrl.webui.util.WebUIOptions
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
import androidx.core.graphics.drawable.toDrawable
import com.dergoogler.mmrl.webui.interfaces.ApplicationInterface
import com.dergoogler.mmrl.webui.interfaces.UserManagerInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.interfaces.WebUIInterface
import com.dergoogler.mmrl.webui.util.addJavascriptInterface

/**
 * A Composable function that displays a WebView for a web-based UI.
 *
 * This function sets up and manages a WebView instance to load and display a web UI. It handles
 * various configurations, including JavaScript interfaces, debugging options, file access, and
 * back button behavior. It also manages loading assets from different sources.
 *
 * @param webView The WebView instance to be used for displaying the web UI.
 * @param options The configuration options for the web UI, including debug settings, URLs, and mod-specific details.
 * @param interfaces A list of JavaScriptInterface objects that provide a bridge between JavaScript and Kotlin code.
 *
 * @see WebUIOptions
 * @see JavaScriptInterface
 * @see WebView
 * @see BackHandler
 * @see AndroidView
 * @see LocalContext
 * @see LocalUriHandler
 * @see MaterialTheme
 * @see CompositionLocalProvider
 * @see LocalInsets
 * @see ViewCompat
 * @see WebUIClient
 *
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebUIScreen(
    webView: WebView,
    options: WebUIOptions,
    interfaces: List<JavaScriptInterface<out WebUIInterface>> = listOf(),
) {
    val context = LocalContext.current
    val showConfirm = rememberConfirm(context)
    val showPrompt = rememberPrompt(context)
    val uriHandler = LocalUriHandler.current
    val colorScheme = MaterialTheme.colorScheme

    WebView.setWebContentsDebuggingEnabled(options.debug)

    BackHandler {
        if (options.config.backHandler && webView.canGoBack()) {
            webView.goBack()
            return@BackHandler
        }

        if (options.config.exitConfirm) {
            showConfirm(
                ConfirmData(
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
                    "/mmrl/" to internalPathHandler(options),
                    "/internal/" to internalPathHandler(options),
                    ".${options.modId.id}/" to suPathHandler("/data/adb/modules/${options.modId.id}".toSuFile()),
                    "/.adb/" to suPathHandler("/data/adb".toSuFile()),
                    "/.config/" to suPathHandler("/data/adb/.config".toSuFile()),
                    "/.local/" to suPathHandler("/data/adb/.local".toSuFile()),
                    "/" to webrootPathHandler(options),
                )
            )

            key(options.recomposeCount) {
                AndroidView(factory = {
                    webView.apply {
                        setBackgroundColor(colorScheme.background.toArgb())
                        background = colorScheme.background.toArgb().toDrawable()
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
                            debug = options.debug,
                            options = options,
                        )

                        webChromeClient = WebUIClient.ChromeClient(
                            context = context,
                            options = options,
                            showPrompt = showPrompt,
                            showConfirm = showConfirm,
                        )

                        for (i in interfaces) {
                            addJavascriptInterface(context, options.modId, i)
                        }

                        val internalInterfaces = listOf(
                            FileInputInterface.factory(),
                            ApplicationInterface.factory(),
                            FileInterface.factory(),
                            ModuleInterface.factory(
                                WXOptions(
                                    context = context,
                                    webView = this,
                                    modId = options.modId
                                ), insets, options
                            ),
                            UserManagerInterface.factory()
                        )

                        for (i in internalInterfaces) {
                            addJavascriptInterface(context, options.modId, i)
                        }
                    }
                }, update = {
                    it.apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            if (options.debug && options.remoteDebug) {
                                mixedContentMode =
                                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            }
                            userAgentString = options.userAgentString
                        }


                        if (options.requireNewAppVersion) {
                            loadData(
                                getRequireNewVersion(context, options), "text/html", "UTF-8"
                            )

                            return@apply
                        }

                        loadUrl(options.domainUrl)
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
    options: WebUIOptions,
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
                href = "${options.domain}/mmrl/insets.css"
            }
            link {
                rel = "stylesheet"
                href = "${options.domain}/mmrl/colors.css"
            }
            link {
                rel = "stylesheet"
                href = "${options.domain}/mmrl/assets/webui/requireNewVersion.css"
            }
            title { +"New App Version Required" }
        }
        body {
            div(classes = "container") {
                div(classes = "content") {
                    div(classes = "title") { +context.getString(R.string.requireNewVersion_cannot_load_webui) }
                    div {
                        b { +options.modId.id }
                        +" "
                        +context.getString(R.string.requireNewVersion_require_text)
                        +" "
                        i { +options.config.require.version.required.toString() }
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

                        val supportLink = options.config.require.version.supportLink
                        val supportText = options.config.require.version.supportText

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