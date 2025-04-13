package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.asResponse
import java.io.IOException

@Composable
fun suPathHandler(
    directory: SuFile,
): (String) -> WebResourceResponse? {
    return handler@{ path ->
        return@handler try {
            SuFile(directory, path).asResponse()
        } catch (e: IOException) {
            Log.e("suPathHandler", "Error opening webroot path: $path", e)
            null
        }
    }
}