package com.dergoogler.mmrl.webui.util

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.interfaces.WebUIInterface
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.model.ModId

@SuppressLint("JavascriptInterface")
internal fun <T : WebUIInterface> WebView.addJavascriptInterface(
    context: Context,
    modId: ModId,
    jsInterface: JavaScriptInterface<T>,
) {
    val js = jsInterface.createNew(WXOptions(context, this, modId))
    return addJavascriptInterface(js.instance, js.name)
}