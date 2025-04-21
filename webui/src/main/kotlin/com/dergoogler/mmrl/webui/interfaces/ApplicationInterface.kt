package com.dergoogler.mmrl.webui.interfaces

import android.webkit.JavascriptInterface
import androidx.core.content.pm.PackageInfoCompat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.webui.model.App
import com.dergoogler.mmrl.webui.model.JavaScriptInterface

class ApplicationInterface(
    wxOptions: WXOptions,
) : WebUIInterface(wxOptions) {
    override var name: String = "webui"
    companion object {
        fun factory() = JavaScriptInterface(ApplicationInterface::class.java)
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
        val versionName = packageInfo.versionName

        return App(
            packageName = packageInfo.packageName,
            versionName = versionName,
            versionCode = versionCode
        )
    }
}