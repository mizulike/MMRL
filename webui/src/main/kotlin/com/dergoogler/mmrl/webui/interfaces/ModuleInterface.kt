package com.dergoogler.mmrl.webui.interfaces

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.app.ShareCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.model.JavaScriptInterface
import com.dergoogler.mmrl.webui.model.WebUIConfig.Companion.toWebUIConfig
import com.dergoogler.mmrl.webui.moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import java.io.BufferedInputStream

@JsonClass(generateAdapter = true)
internal data class Manager(
    val name: String,
    val versionName: String,
    val versionCode: Int,
)

@Keep
class ModuleInterface(
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    override var name: String = "$${modId.sanitizedId}"

    companion object {
        fun factory() = JavaScriptInterface(ModuleInterface::class.java)
    }

    private fun getWindowInsetsController(activity: Activity): WindowInsetsControllerCompat =
        WindowCompat.getInsetsController(
            activity.window,
            webView
        )

    init {
        activity<Unit> {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            getWindowInsetsController(this).systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    }

    private var managerAdapter: JsonAdapter<Manager> = moshi.adapter(Manager::class.java)

    @get:JavascriptInterface
    val manager: String
        get() {
            deprecated("$name.getManager()", "webui.getCurrentRootManager()")

            return managerAdapter.toJson(
                Manager(
                    name = Platform.platform.name,
                    versionName = Platform.moduleManager.version,
                    versionCode = Platform.moduleManager.versionCode
                )
            )
        }

    @get:JavascriptInterface
    val mmrl: String
        get() {
            deprecated("$name.getMmrl()", "webui.getCurrentApplication()")

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            val versionName = packageInfo.versionName ?: "unknown"

            return managerAdapter.toJson(
                Manager(
                    name = packageInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode.toInt()
                )
            )
        }

    @get:JavascriptInterface
    val hasAccessToFileSystem: Boolean
        get() {
            deprecated("$name.getHasAccessToFileSystem()")
            return true
        }

    @get:JavascriptInterface
    val hasAccessToAdvancedKernelSuAPI: Boolean
        get() {
            deprecated("$name.getHasAccessToAdvancedKernelSuAPI()")
            return true
        }

    @Deprecated("Use window.getComputedStyle(document.body).getPropertyValue('--window-inset-top') instead")
    @get:JavascriptInterface
    val windowTopInset: Int
        get() {
            deprecated(
                "$name.getWindowTopInset()",
                "window.getComputedStyle(document.body).getPropertyValue('--window-inset-top')"
            )
            return -1
        }

    @Deprecated("Use window.getComputedStyle(document.body).getPropertyValue('--window-inset-bottom') instead")
    @get:JavascriptInterface
    val windowBottomInset: Int
        get() {
            deprecated(
                "$name.getWindowBottomInset()",
                "window.getComputedStyle(document.body).getPropertyValue('--window-inset-bottom')"
            )
            return -1
        }

    @Deprecated("Use window.getComputedStyle(document.body).getPropertyValue('--window-inset-left') instead")
    @get:JavascriptInterface
    val windowLeftInset: Int
        get() {
            deprecated(
                "$name.getWindowLeftInset()",
                "window.getComputedStyle(document.body).getPropertyValue('--window-inset-left')"
            )
            return -1
        }

    @Deprecated("Use window.getComputedStyle(document.body).getPropertyValue('--window-inset-right') instead")
    @get:JavascriptInterface
    val windowRightInset: Int
        get() {
            deprecated(
                "$name.getWindowRightInset()",
                "window.getComputedStyle(document.body).getPropertyValue('--window-inset-right')"
            )
            return -1
        }

    @get:JavascriptInterface
    val isLightNavigationBars: Boolean?
        get() = activity<Boolean> {
            getWindowInsetsController(this).isAppearanceLightNavigationBars
        }

    @get:JavascriptInterface
    val isDarkMode: Boolean
        get() = options.isDarkMode

    @JavascriptInterface
    fun setLightNavigationBars(isLight: Boolean) = runOnUiThread {
        getWindowInsetsController(this).isAppearanceLightNavigationBars = isLight
    }

    @get:JavascriptInterface
    val isLightStatusBars: Boolean?
        get() = activity {
            getWindowInsetsController(this).isAppearanceLightStatusBars
        }

    @JavascriptInterface
    fun setLightStatusBars(isLight: Boolean) = runOnUiThread {
        getWindowInsetsController(this).isAppearanceLightStatusBars = isLight
    }

    @get:JavascriptInterface
    val sdk: Int get() = Build.VERSION.SDK_INT

    @JavascriptInterface
    fun shareText(text: String) {
        ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(text)
            .startChooser()
    }

    @JavascriptInterface
    fun shareText(text: String, type: String) {
        ShareCompat.IntentBuilder(context)
            .setType(type)
            .setText(text)
            .startChooser()
    }

    @get:JavascriptInterface
    val recomposeCount get() = options.recomposeCount

    @JavascriptInterface
    fun recompose() = options.recomposeCount++

    @JavascriptInterface
    fun requestAdvancedKernelSUAPI() {
        deprecated("$name.requestAdvancedKernelSUAPI()")
    }

    @JavascriptInterface
    fun requestFileSystemAPI() {
        deprecated("$name.requestFileSystemAPI()")
    }

    @JavascriptInterface
    fun createShortcut(title: String?, icon: String?) {
        if (title == null || icon == null) {
            Toast.makeText(
                context,
                context.getString(R.string.title_or_icon_not_found),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        createShortcutInternal(title, icon)
    }

    @JavascriptInterface
    fun createShortcut() {
        val config = modId.toWebUIConfig()
        val title = config.title
        val icon = config.icon

        if (title == null || icon == null) {
            Toast.makeText(
                context,
                context.getString(R.string.title_or_icon_not_found),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        createShortcutInternal(title, icon)
    }

    private val shortcutManager = context.getSystemService(ShortcutManager::class.java)

    @JavascriptInterface
    fun hasShortcut(): Boolean {
        val id = modId.id
        val shortcutId = "shortcut_$id"
        return shortcutManager.pinnedShortcuts.any { it.id == shortcutId }
    }

    private fun createShortcutInternal(title: String, icon: String) {

        if (options.cls == null) {
            Toast.makeText(
                context,
                context.getString(R.string.class_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }

        val id = modId.id
        val shortcutId = "shortcut_$id"
        val webRoot = SuFile("/data/adb/modules/$id/webroot")
        val iconFile = SuFile(webRoot, icon)

        if (!iconFile.exists()) {
            Log.d("createShortcutInternal", "Icon not found: $iconFile")
            Toast.makeText(context, context.getString(R.string.icon_not_found), Toast.LENGTH_SHORT)
                .show()
            return
        }


        if (!shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(
                context,
                context.getString(R.string.shortcut_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }

        if (hasShortcut()) {
            Toast.makeText(
                context,
                context.getString(R.string.shortcut_already_exists),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shortcutIntent = Intent(context, options.cls).apply {
            action = Intent.ACTION_VIEW
            putExtra("MOD_ID", id)
        }

        val bis = BufferedInputStream(iconFile.newInputStream())
        val bitmap = BitmapFactory.decodeStream(bis)

        val shortcut = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(title)
            .setLongLabel(title)
            .setIcon(Icon.createWithAdaptiveBitmap(bitmap))
            .setIntent(shortcutIntent)
            .build()

        shortcutManager.requestPinShortcut(shortcut, null)
    }
}