package com.dergoogler.webui.plugin

import android.content.Context
import android.webkit.WebView
import androidx.annotation.Keep
import com.dergoogler.mmrl.Platform
import dev.dergoogler.mmrl.compat.stub.IFileManager
import kotlinx.coroutines.CoroutineScope

@Keep
data class Plugin(
    val modId: String,
    val context: Context,
    val webView: WebView,
    val fileManager: IFileManager,
    val platform: Platform,
    val isProviderAlive: Boolean,
    val viewModelScope: CoroutineScope
) {
    val sanitizedModId: String
        get() {
            return modId.replace(Regex("[^a-zA-Z0-9._]"), "_")
        }
}