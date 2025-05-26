package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import com.dergoogler.mmrl.webui.MimeUtil
import com.dergoogler.mmrl.webui.PathHandler
import com.dergoogler.mmrl.webui.notFoundResponse
import com.dergoogler.mmrl.webui.util.WebUIOptions
import java.io.IOException

fun assetsPathHandler(options: WebUIOptions): PathHandler {
    val assetHelper = options.context.assets
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
