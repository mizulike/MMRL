package com.dergoogler.mmrl.ui.screens.repositories.screens.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event.Companion.isFailed
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.network.compose.requestString
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.viewmodel.ModuleViewModel
import androidx.navigation.NavHostController
import com.dergoogler.mmrl.ui.activity.webui.interfaces.MarkdownInterface
import com.dergoogler.mmrl.webui.client.WXClient
import com.dergoogler.mmrl.webui.handler.internalPathHandler
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WXView
import com.dergoogler.mmrl.webui.wxAssetLoader

const val launchUrl = "https://mui.kernelsu.org/internal/assets/markdown.html"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ViewDescriptionScreen(
    viewModel: ModuleViewModel,
    navController: NavHostController,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current

    var readme by remember { mutableStateOf("") }
    val event = requestString(
        url = viewModel.online.readme,
        onSuccess = { readme = it }
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
                        val options = WebUIOptions(
                            context = context,
                          /*  client = { o, i ->
                                val assetsLoader = wxAssetLoader(
                                    handlers = buildList {
                                        add("/internal/" to internalPathHandler(o, i))
                                    }
                                )

                                return@WebUIOptions WXClient(o, assetsLoader)
                            }*/
                        )

                        WXView(options).apply {
                            addJavascriptInterface(MarkdownInterface.factory(readme))
                        }
                    },
                    update = {
                        it.loadUrl(launchUrl)
                    }
                )
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