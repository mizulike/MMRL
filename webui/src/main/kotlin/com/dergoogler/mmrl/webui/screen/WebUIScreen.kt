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

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
/**
 * Represents a screen for displaying web content within the application.
 *
 * @param webView The required WebView instance used to render web content.
 * @param options The required configuration options for the WebView.
 * @param interfaces Optional additional JavaScript interfaces that can be added to the WebView.
 */
fun WebUIScreen(
    webView: WebView,
    options: WebUIOptions,
    interfaces: List<JavaScriptInterface> = listOf(),
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
                    "/mmrl/" to internalPathHandler(options),
                    "/internal/" to internalPathHandler(options),
                    ".${options.modId}/" to suPathHandler("/data/adb/modules/${options.modId}".toSuFile()),
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
                            addJavascriptInterface(
                                i.instance, i.name
                            )
                        }

                        addJavascriptInterface(
                            FileInputInterface(
                                context = context,
                                webView = this,
                            ), options.sanitizedModIdWithFileInputStream
                        )

                        addJavascriptInterface(
                            ModuleInterface(
                                options = options,
                                context = context,
                                webView = this,
                                insets = insets
                            ), "$${options.sanitizedModId}"
                        )

                        addJavascriptInterface(
                            FileInterface(this, context), options.sanitizedModIdWithFile
                        )
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
                            userAgentString = "DON'T TRACK ME DOWN MOTHERFUCKER!"
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
                        b { +options.modId }
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