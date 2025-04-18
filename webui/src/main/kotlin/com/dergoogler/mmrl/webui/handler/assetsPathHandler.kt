package com.dergoogler.webui.handlers

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.mmrl.webui.MimeUtil
import com.dergoogler.mmrl.webui.notFoundResponse
import java.io.IOException

@Composable
/**
 * Handles the retrieval of web assets by providing a function that maps a given asset path
 * to a corresponding `WebResourceResponse`.
 *
 * @return A lambda function that takes a `String` representing the asset path and returns
 *         a `WebResourceResponse` containing the requested asset's data.
 */
fun assetsPathHandler(): (String) -> WebResourceResponse {
    val context = LocalContext.current

    val assetHelper = context.assets
    return handler@{ path ->
        try {
            val inputStream = assetHelper.open(path.removePrefix("/"))
            val mimeType = MimeUtil.getMimeFromFileName(path)
            return@handler WebResourceResponse(mimeType, null, inputStream)
        } catch (e: IOException) {
            Log.e("assetsPathHandler", "Error opening asset path: $path", e)
            return@handler notFoundResponse
        }
    }
}
