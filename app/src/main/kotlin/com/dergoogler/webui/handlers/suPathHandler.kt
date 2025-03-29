package com.dergoogler.webui.handlers

import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.webui.core.asResponse
import com.dergoogler.webui.core.noResponse
import timber.log.Timber
import java.io.IOException

@Composable
fun suPathHandler(
    directory: SuFile,
): (String) -> WebResourceResponse {
    return { path ->
        try {
            val file = SuFile(directory, path)

            if (!file.exists()) {
                Timber.e("File not found: %s", file.absolutePath)
                noResponse
            }

            file.asResponse()
        } catch (e: IOException) {
            Timber.e(e, "Error opening webroot path: $path")
            noResponse
        }
    }
}