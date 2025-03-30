package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.webui.core.MimeUtil
import com.dergoogler.webui.core.notFoundResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun assetsPathHandler(): (String) -> WebResourceResponse {
    val context = LocalContext.current

    val assetHelper = context.assets
    return handler@{ path ->
        try {
            val inputStream = assetHelper.open(path.removePrefix("/"))
            val mimeType = MimeUtil.getMimeFromFileName(path)
            return@handler WebResourceResponse(mimeType, null, inputStream)
        } catch (e: IOException) {
            Timber.e(e, "Error opening asset path: $path")
            return@handler notFoundResponse
        }
    }
}
