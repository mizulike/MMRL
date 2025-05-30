package com.dergoogler.mmrl.webui.interfaces

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import androidx.core.content.pm.PackageInfoCompat
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.webui.model.App

@Keep
class ApplicationInterface(
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    override var name: String = "webui"
    override var tag: String = "ApplicationInterface"

    @JavascriptInterface
    fun exit() {
        withActivity<Unit> {
            finish()
        }
    }

    @JavascriptInterface
    fun setRefreshing(state: Boolean) {
        if (!config.pullToRefresh) {
            console.error(
                Exception("Pull-To-Refresh needs to be enable in order to use $name.setRefreshing(boolean)")
            )
            return
        }

        if (!config.useJavaScriptRefreshInterceptor) {
            console.error(
                Exception("$name.setRefreshing(boolean) is not supported with native refresh interceptor")
            )
            return
        }

        val swipeLayout = webView.mSwipeView

        if (swipeLayout == null) {
            console.error(Exception("WXSwipeRefresh not found"))
            return
        }

        post {
            swipeLayout.isRefreshing = state
        }
    }

    @get:JavascriptInterface
    val currentRootManager: App
        get() = App(
            packageName = PlatformManager.platform.name,
            versionName = PlatformManager.moduleManager.version,
            versionCode = PlatformManager.moduleManager.versionCode.toLong()
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