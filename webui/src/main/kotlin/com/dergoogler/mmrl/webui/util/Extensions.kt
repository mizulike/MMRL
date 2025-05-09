package com.dergoogler.mmrl.webui.util

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.platform.model.ModId

@SuppressLint("JavascriptInterface")
internal fun <T : WXInterface> WebView.addJavascriptInterface(
    context: Context,
    modId: ModId,
    jsInterface: JavaScriptInterface<T>,
) {
    val js = jsInterface.createNew(WXOptions(context, this, modId))
    return addJavascriptInterface(js.instance, js.name)
}