package com.dergoogler.mmrl.webui.interfaces

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.core.content.pm.PackageInfoCompat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.webui.model.App
import com.dergoogler.mmrl.webui.model.JavaScriptInterface

@Keep
class ApplicationInterface(
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    override var name: String = "webui"

    companion object {
        fun factory() = JavaScriptInterface(ApplicationInterface::class.java)
    }

    @JavascriptInterface
    fun exit() {
        activity<Unit> {
            finish()
        }
    }

    @JavascriptInterface
    fun setRefreshing(state: Boolean) {
        if (!config.pullToRefresh) {
            throwJsError(Exception("Pull-To-Refresh needs to be enable in order to use $name.setRefreshing(boolean)"))
            return
        }

        if (!config.useJavaScriptRefreshInterceptor) {
            throwJsError(Exception("$name.setRefreshing(boolean) is not supported with native refresh interceptor"))
            return
        }

        val swipeLayout = webView.mSwipeView

        if (swipeLayout == null) {
            throwJsError(Exception("WXSwipeRefresh not found"))
            return
        }

        runOnUiThread {
            swipeLayout.isRefreshing = state
        }
    }

    @get:JavascriptInterface
    val currentRootManager: App
        get() = App(
            packageName = Platform.platform.name,
            versionName = Platform.moduleManager.version,
            versionCode = Platform.moduleManager.versionCode.toLong()
        )

    @get:JavascriptInterface
    val currentApplication: App
        get() = getApplication(context.packageName)

    @JavascriptInterface
    fun getApplication(packageName: String): App {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        val versionName = packageInfo.versionName ?: "unknown"

        return App(
            packageName = packageInfo.packageName,
            versionName = versionName,
            versionCode = versionCode
        )
    }
}