package com.dergoogler.mmrl.ui.screens.repositories.screens.view

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event.Companion.isFailed
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.network.compose.requestString
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.viewmodel.ModuleViewModel
import com.dergoogler.mmrl.webui.Insets
import com.dergoogler.mmrl.webui.LocalInsets
import com.dergoogler.mmrl.webui.handler.internalPathHandler
import com.dergoogler.mmrl.webui.rememberWebUIAssetLoader
import dev.dergoogler.mmrl.compat.core.LocalUriHandler
import androidx.core.graphics.drawable.toDrawable

const val launchUrl = "https://mui.kernelsu.org/internal/assets/markdown.html"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ViewDescriptionScreen(
    viewModel: ModuleViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val browser = LocalUriHandler.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var readme by remember { mutableStateOf("") }
    val event = requestString(
        url = viewModel.online.readme,
        onSuccess = { readme = it }
    )

    CompositionLocalProvider(
        LocalInsets provides Insets.None
    ) {
        val webUiAssetLoader = rememberWebUIAssetLoader(
            handlers = listOf(
                "/internal/" to internalPathHandler(),
            )
        )

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    navController = navController
                )
            },
            contentWindowInsets = WindowInsets.none
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = event.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Loading()
                }

                AnimatedVisibility(
                    visible = event.isFailed,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Failed()
                }

                AnimatedVisibility(
                    visible = event.isSucceeded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                setBackgroundColor(colorScheme.background.toArgb())
                                background = colorScheme.background.toArgb().toDrawable()
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                ViewCompat.setOnApplyWindowInsetsListener(this) { _, _ ->
                                    WindowInsetsCompat.CONSUMED
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView,
                                        request: WebResourceRequest?,
                                    ): Boolean {
                                        val mUrl = request?.url?.toString() ?: return false

                                        return if (launchUrl != mUrl) {
                                            browser.openUri(
                                                uri = mUrl,
                                                onSuccess = { intent, uri ->
                                                    intent.launchUrl(context, uri.toUri())
                                                }
                                            )
                                            true
                                        } else {
                                            view.loadUrl(mUrl)
                                            false
                                        }
                                    }

                                    override fun shouldInterceptRequest(
                                        view: WebView,
                                        request: WebResourceRequest,
                                    ): WebResourceResponse? {
                                        return webUiAssetLoader(request.url)
                                    }
                                }

                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun get() = readme
                                    },
                                    "markdown"
                                )
                            }
                        },
                        update = {
                            it.apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    allowFileAccess = false
                                    userAgentString = "DON'T TRACK ME DOWN MOTHERFUCKER!"
                                }

                                loadUrl(launchUrl)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) = TopAppBar(
    navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left), contentDescription = null
            )
        }
    },
    title = { Text(text = stringResource(id = R.string.view_module_about_this_module)) },
    scrollBehavior = scrollBehavior
)