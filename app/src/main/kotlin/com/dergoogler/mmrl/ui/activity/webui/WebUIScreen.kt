package com.dergoogler.mmrl.ui.activity.webui

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
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.developerMode
import com.dergoogler.mmrl.ui.activity.webui.handlers.MMRLWebClient
import com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu.AdvancedKernelSUAPI
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.FileInterface
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.MMRLInterface
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.VersionInterface
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.core.LocalInsets
import com.dergoogler.webui.core.rememberInsets
import com.dergoogler.webui.core.rememberWebUIAssetLoader
import com.dergoogler.webui.handlers.mmrlPathHandler
import com.dergoogler.webui.handlers.suPathHandler
import com.dergoogler.webui.handlers.webrootPathHandler
import dev.dergoogler.mmrl.compat.core.MMRLUriHandlerImpl
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


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebUIScreen(
    webView: WebView,
    viewModel: WebUIViewModel,
) {
    val context = LocalContext.current
    val userPrefs = LocalUserPreferences.current
    val showConfirm = rememberConfirm()
    val browser = LocalUriHandler.current as MMRLUriHandlerImpl
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = userPrefs.isDarkMode()

    WebView.setWebContentsDebuggingEnabled(userPrefs.developerMode)

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
            return@BackHandler
        }

        if (viewModel.config.exitConfirm) {
            showConfirm(ConfirmData(
                title = R.string.exit,
                description = R.string.are_you_sure_you_want_to_exit,
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
                    "/mmrl/" to mmrlPathHandler(),
                    ".${viewModel.modId}/" to suPathHandler("/data/adb/modules/${viewModel.modId}".toSuFile()),
                    "/.adb/" to suPathHandler("/data/adb".toSuFile()),
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

                        if (viewModel.config.hasPluginDexLoaderPermission) {
                            viewModel.loadDexPluginsFromMemory(context, this)
                        }

                        webViewClient = MMRLWebClient(
                            context = context,
                            browser = browser,
                            webuiAssetsLoader = webuiAssetsLoader,
                            userPrefs = userPrefs,
                            viewModel = viewModel,
                        )

                        addJavascriptInterface(
                            VersionInterface(
                                context = context,
                                webView = this,
                                viewModel = viewModel,
                            ), "mmrl"
                        )
                    }
                }, update = {
                    it.apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            userPrefs.developerMode({ useWebUiDevUrl }) {
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

                        addJavascriptInterface(
                            MMRLInterface(
                                viewModel = viewModel,
                                context = context,
                                isDark = isDarkMode,
                                webView = this,
                            ), "$${viewModel.sanitizedModId}"
                        )

                        addJavascriptInterface(
                            AdvancedKernelSUAPI(context, this, userPrefs), "ksu"
                        )

                        addJavascriptInterface(
                            FileInterface(this, context), viewModel.sanitizedModIdWithFile
                        )

                        val dsl = viewModel.loadDslDex(context, webView)

                        if (dsl != null) {
                            loadData(dsl, "text/html", "UTF-8")
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
                href = "https://mui.kernelsu.org/mmrl/insets.css"
            }
            link {
                rel = "stylesheet"
                href = "https://mui.kernelsu.org/mmrl/colors.css"
            }
            link {
                rel = "stylesheet"
                href = "https://mui.kernelsu.org/mmrl/assets/webui/requireNewVersion.css"
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