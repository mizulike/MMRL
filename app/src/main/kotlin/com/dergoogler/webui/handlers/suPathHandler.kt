package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.webui.core.asResponse
import com.dergoogler.webui.core.notFoundResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun suPathHandler(
    directory: SuFile,
): (String) -> WebResourceResponse {
    return handler@{ path ->
        return@handler try {
            SuFile(directory, path).asResponse()
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            notFoundResponse
        }
    }
}