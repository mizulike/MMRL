package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.asResponse
import java.io.IOException

@Composable
/**
 * Handles the processing of a specific path for the WebUI.
 *
 * This function is responsible for managing and responding to requests
 * related to a particular path in the application. It may include logic
 * for routing, validation, and response generation.
 *
 * @param <Add relevant parameters here if applicable>
 * @return <Describe the return value if applicable>
 */
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