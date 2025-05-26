package com.dergoogler.mmrl.webui.handler

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.PathHandler
import com.dergoogler.mmrl.webui.asResponse
import java.io.IOException

/**
 * Creates a handler function that resolves web resources from a specified directory.
 *
 * This function returns a lambda that takes a path (String) as input. When invoked, this lambda
 * attempts to locate and open the corresponding file within the provided `directory`.
 * If successful, it returns the file's contents as a [WebResourceResponse]. If an error occurs,
 * such as the file not being found or an I/O error during access, it logs the error and returns `null`.
 *
 * @param directory The base directory (of type [SuFile]) from which to resolve paths. All paths
 *                  passed to the returned handler will be considered relative to this directory.
 * @return A lambda function of type `(String) -> WebResourceResponse?`. This lambda takes a
 *         path (String) relative to the `directory` and returns a [WebResourceResponse] representing
 *         the file contents, or `null` if an error occurred.
 *
 * @throws IOException if an error occurs during file access.
 *
 * @sample
 * ```kotlin
 * // Assuming 'rootDirectory' is a valid SuFile instance representing the web root.
 * val myHandler = suPathHandler(rootDirectory)
 *
 * // Later, when you have a path, you can use the handler:
 * val response = myHandler("index.html") // Attempts to open 'rootDirectory/index.html'
 * if (response != null) {
 *   // Process the WebResourceResponse
 * } else {
 *   // Handle the error (file not found, etc.)
 * }
 *
 * val response2 = myHandler("images/logo.png") // Attempts to open 'rootDirectory/images/logo.png'
 * ```
 */
fun suPathHandler(
    directory: SuFile,
): PathHandler {
    return handler@{ path ->
        return@handler try {
            SuFile(directory, path).asResponse()
        } catch (e: IOException) {
            Log.e("suPathHandler", "Error opening webroot path: $path", e)
            null
        }
    }
}