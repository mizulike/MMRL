package com.dergoogler.mmrl.ui.activity.webui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.developerMode
import com.dergoogler.mmrl.ui.activity.webui.handlers.MMRLWebClient
import com.dergoogler.mmrl.ui.activity.webui.handlers.MMRLWebUIHandler
import com.dergoogler.mmrl.ui.activity.webui.handlers.SuFilePathHandler
import com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu.AdvancedKernelSUAPI
import com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu.BaseKernelSUAPI
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.FileInterface
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.MMRLInterface
import com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl.VersionInterface
import com.dergoogler.mmrl.ui.component.ConfirmDialog
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.SettingsViewModel
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
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
import timber.log.Timber


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebUIScreen(
    viewModel: WebUIViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val userPrefs = LocalUserPreferences.current
    val density = LocalDensity.current
    val browser = LocalUriHandler.current as MMRLUriHandlerImpl
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val filledTonalButtonColors = ButtonDefaults.filledTonalButtonColors()
    val cardColors = CardDefaults.cardColors()
    val isDarkMode = userPrefs.isDarkMode()
    val layoutDirection = LocalLayoutDirection.current

    val webView = WebView(context)
    WebView.setWebContentsDebuggingEnabled(userPrefs.developerMode)

    val insets = WindowInsets.systemBars
    LaunchedEffect(density, layoutDirection, insets) {
        viewModel.initInsets(density, layoutDirection, insets)
        Timber.d("Insets calculated: top = ${viewModel.topInset}, bottom = ${viewModel.bottomInset}, left = ${viewModel.leftInset}, right = ${viewModel.rightInset}")
    }

    val allowedFsApi = viewModel.modId in userPrefs.allowedFsModules
    val allowedKsuApi = viewModel.modId in userPrefs.allowedKsuModules

    if (!allowedKsuApi && !viewModel.hasRequestedAdvancedKernelSUAPI && viewModel.dialogRequestAdvancedKernelSUAPI) {
        ConfirmDialog(
            title = stringResource(R.string.allow_advanced_kernelsu_api),
            description = stringResource(R.string.allow_advanced_kernelsu_api_desc),
            onClose = {
                viewModel.hasRequestedAdvancedKernelSUAPI = true
                viewModel.dialogRequestAdvancedKernelSUAPI = false
            },
            onConfirm = {
                viewModel.dialogRequestAdvancedKernelSUAPI = false
                val newModules = userPrefs.allowedKsuModules + viewModel.modId
                settingsViewModel.setAllowedKsuModules(newModules)
                viewModel.recomposeCount++
            }
        )
    }

    if (viewModel.config.hasFileSystemPermission && !allowedFsApi && !viewModel.hasRequestFileSystemAPI && viewModel.dialogRequestFileSystemAPI) {
        ConfirmDialog(
            title = stringResource(R.string.allow_filesystem_api),
            description = stringResource(R.string.allow_filesystem_api_desc),
            onClose = {
                viewModel.hasRequestFileSystemAPI = true
                viewModel.dialogRequestFileSystemAPI = false
            },
            onConfirm = {
                viewModel.dialogRequestFileSystemAPI = false
                val newModules = userPrefs.allowedFsModules + viewModel.modId
                settingsViewModel.setAllowedFsModules(newModules)
                viewModel.recomposeCount++
            }
        )
    }

    if (viewModel.topInset != null && viewModel.bottomInset != null) {
        val webViewAssetLoader = remember(viewModel.topInset, viewModel.bottomInset) {
            WebViewAssetLoader.Builder()
                .setDomain("mui.kernelsu.org")
                .addPathHandler(
                    "/",
                    SuFilePathHandler(
                        viewModel = viewModel,
                    )
                )
                .addPathHandler(
                    "/mmrl/assets/",
                    WebViewAssetLoader.AssetsPathHandler(context)
                )
                .addPathHandler(
                    "/mmrl/",
                    MMRLWebUIHandler(
                        viewModel = viewModel,
                        colorScheme = colorScheme,
                        typography = typography,
                        filledTonalButtonColors = filledTonalButtonColors,
                        cardColors = cardColors
                    )
                )
                .build()
        }

        key(viewModel.recomposeCount) {
            AndroidView(
                factory = {
                    webView.apply {
                        setBackgroundColor(colorScheme.background.toArgb())
                        background = ColorDrawable(colorScheme.background.toArgb())
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        ViewCompat.setOnApplyWindowInsetsListener(this) { _, _ ->
                            WindowInsetsCompat.CONSUMED
                        }

                        if (viewModel.config.hasPluginDexLoaderPermission) {
                            viewModel.loadDexPluginsFromMemory(context, this)
                        }

                        webViewClient = MMRLWebClient(
                            context = context,
                            browser = browser,
                            webViewAssetLoader = webViewAssetLoader,
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
                },
                update = {
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

                        addJavascriptInterface(
                            MMRLInterface(
                                viewModel = viewModel,
                                context = context,
                                isDark = isDarkMode,
                                webView = this,
                                allowedFsApi = allowedFsApi,
                                allowedKsuApi = allowedKsuApi
                            ), "$${viewModel.sanitizedModId}"
                        )

                        addJavascriptInterface(
                            if (allowedKsuApi) {
                                AdvancedKernelSUAPI(context, this, userPrefs)
                            } else {
                                BaseKernelSUAPI(context, this)
                            }, "ksu"
                        )

                        if (viewModel.config.hasFileSystemPermission && allowedFsApi) {
                            addJavascriptInterface(
                                FileInterface(this, context),
                                viewModel.sanitizedModIdWithFile
                            )
                        }

                        if (viewModel.requireNewAppVersion) {
                            loadData(getRequireNewVersion(context, viewModel), "text/html", "UTF-8")

                            return@apply
                        }

                        val dsl = viewModel.loadDslDex(context, webView)

                        if (dsl != null) {
                            loadData(dsl, "text/html", "UTF-8")
                            return@apply
                        }

                        loadUrl(viewModel.domainUrl)
                    }
                }
            )
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
                href =
                    "https://mui.kernelsu.org/mmrl/assets/webui/requireNewVersion.css"
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