package com.dergoogler.mmrl.webui.interfaces

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.Insets
import com.dergoogler.mmrl.webui.R
import com.dergoogler.mmrl.webui.model.WebUIConfig.Companion.toWebUiConfig
import com.dergoogler.mmrl.webui.viewModel.WebUIViewModel
import com.squareup.moshi.JsonClass
import java.io.BufferedInputStream

@JsonClass(generateAdapter = true)
internal data class Manager(
    val name: String,
    val versionName: String,
    val versionCode: Int,
)

class ModuleInterface(
    context: Context,
    webView: WebView,
    private val insets: Insets,
    private val viewModel: WebUIViewModel,
) : WebUIInterface(webView, context) {
    private var windowInsetsController: WindowInsetsControllerCompat =
        WindowCompat.getInsetsController(
            activity.window,
            webView
        )

    init {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @get:JavascriptInterface
    val hasAccessToFileSystem: Boolean
        get() = true

    @get:JavascriptInterface
    val hasAccessToAdvancedKernelSuAPI: Boolean
        get() = true

    @get:JavascriptInterface
    val windowTopInset: Int
        get() = insets.top

    @get:JavascriptInterface
    val windowBottomInset: Int
        get() = insets.bottom

    @get:JavascriptInterface
    val windowLeftInset: Int
        get() = insets.left

    @get:JavascriptInterface
    val windowRightInset: Int
        get() = insets.right

    @get:JavascriptInterface
    val isLightNavigationBars: Boolean
        get() = windowInsetsController.isAppearanceLightNavigationBars

    @get:JavascriptInterface
    val isDarkMode: Boolean
        get() = viewModel.isDarkMode

    @JavascriptInterface
    fun setLightNavigationBars(isLight: Boolean) = runOnUiThread {
        windowInsetsController.isAppearanceLightNavigationBars = isLight
    }

    @get:JavascriptInterface
    val isLightStatusBars: Boolean
        get() = windowInsetsController.isAppearanceLightStatusBars

    @JavascriptInterface
    fun setLightStatusBars(isLight: Boolean) = runOnUiThread {
        windowInsetsController.isAppearanceLightStatusBars = isLight
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
    val recomposeCount get() = viewModel.recomposeCount

    @JavascriptInterface
    fun recompose() = viewModel.recomposeCount++

    @JavascriptInterface
    fun requestAdvancedKernelSUAPI() {
        console.info("requestAdvancedKernelSUAPI() is deprecated")
    }

    @JavascriptInterface
    fun requestFileSystemAPI() {
        console.info("requestFileSystemAPI() is deprecated")
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
        val config = viewModel.modId.toWebUiConfig()
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

    private fun createShortcutInternal(title: String, icon: String) {

        if (viewModel.cls == null) {
            Toast.makeText(
                context,
                context.getString(R.string.class_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }

        val id = viewModel.modId
        val shortcutId = "shortcut_$id"
        val webRoot = SuFile("/data/adb/modules/$id/webroot")
        val iconFile = SuFile(webRoot, icon)

        if (!iconFile.exists()) {
            Log.d("createShortcutInternal", "Icon not found: $iconFile")
            Toast.makeText(context, context.getString(R.string.icon_not_found), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)

        if (!shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(
                context,
                context.getString(R.string.shortcut_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }

        if (shortcutManager.pinnedShortcuts.any { it.id == shortcutId }) {
            Toast.makeText(
                context,
                context.getString(R.string.shortcut_already_exists),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shortcutIntent = Intent(context, viewModel.cls).apply {
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