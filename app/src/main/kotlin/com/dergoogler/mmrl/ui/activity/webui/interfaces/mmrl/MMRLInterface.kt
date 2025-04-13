package com.dergoogler.mmrl.ui.activity.webui.interfaces.mmrl

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.moshi
import com.dergoogler.mmrl.ui.activity.webui.WebUIActivity
import com.dergoogler.mmrl.utils.file.SuFile
import com.dergoogler.mmrl.viewmodel.WebUIViewModel
import com.dergoogler.webui.model.WebUIConfig.Companion.toWebUiConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import dev.dergoogler.mmrl.compat.core.MMRLWebUIInterface
import dev.dergoogler.mmrl.compat.ext.shareText
import timber.log.Timber
import java.io.BufferedInputStream

@JsonClass(generateAdapter = true)
internal data class Manager(
    val name: String,
    val versionName: String,
    val versionCode: Int,
)

class MMRLInterface(
    context: Context,
    private val isDark: Boolean,
    webView: WebView,
    private val viewModel: WebUIViewModel,
) : MMRLWebUIInterface(webView, context) {
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

    private var managerAdapter: JsonAdapter<Manager> = moshi.adapter(Manager::class.java)

    @get:JavascriptInterface
    val manager: String
        get() = managerAdapter.toJson(
            Manager(
                name = viewModel.platform.current,
                versionName = viewModel.versionName,
                versionCode = viewModel.versionCode
            )
        )

    @get:JavascriptInterface
    val mmrl: String
        get() = managerAdapter.toJson(
            Manager(
                name = BuildConfig.APPLICATION_ID,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE
            )
        )

    @get:JavascriptInterface
    val hasAccessToFileSystem: Boolean
        get() = true

    @get:JavascriptInterface
    val hasAccessToAdvancedKernelSuAPI: Boolean
        get() = true

    @get:JavascriptInterface
    val windowTopInset: Int?
        get() = viewModel.topInset

    @get:JavascriptInterface
    val windowBottomInset: Int?
        get() = viewModel.bottomInset

    @get:JavascriptInterface
    val windowLeftInset: Int?
        get() = viewModel.leftInset

    @get:JavascriptInterface
    val windowRightInset: Int?
        get() = viewModel.rightInset

    @get:JavascriptInterface
    val isLightNavigationBars: Boolean
        get() = windowInsetsController.isAppearanceLightNavigationBars

    @get:JavascriptInterface
    val isDarkMode: Boolean
        get() = isDark

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
        context.shareText(text)
    }

    @JavascriptInterface
    fun shareText(text: String, type: String) {
        context.shareText(text, type)
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
        val id = viewModel.modId
        val shortcutId = "shortcut_$id"
        val webRoot = SuFile("/data/adb/modules/$id/webroot")
        val iconFile = SuFile(webRoot, icon)

        if (!iconFile.exists()) {
            Timber.d("Icon not found: $iconFile")
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

        val shortcutIntent = Intent(context, WebUIActivity::class.java).apply {
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