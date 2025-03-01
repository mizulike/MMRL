package com.dergoogler.mmrl.ui.activity.webui.interfaces.ksu

import android.content.Context
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.dergoogler.mmrl.compat.core.MMRLWebUIInterface
import org.json.JSONObject

open class BaseKernelSUAPI(
    context: Context,
    webView: WebView,
) : MMRLWebUIInterface(webView, context) {

    @JavascriptInterface
    fun mmrl(): Boolean {
        return true
    }

    @JavascriptInterface
    fun toast(msg: String) {
        runPost {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun fullScreen(enable: Boolean) {
        runMainLooperPost {
            if (enable) {
                hideSystemUI(activity.window)
            } else {
                showSystemUI(activity.window)
            }
        }
    }

    @JavascriptInterface
    fun moduleInfo(): String {
        console.warn("ksu.moduleInfo() have been removed due to security reasons.")
        val currentModuleInfo = JSONObject()
        currentModuleInfo.put("moduleDir", null)
        currentModuleInfo.put("id", null)
        return currentModuleInfo.toString()
    }
}

fun hideSystemUI(window: Window) =
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

fun showSystemUI(window: Window) =
    WindowInsetsControllerCompat(
        window,
        window.decorView
    ).show(WindowInsetsCompat.Type.systemBars())