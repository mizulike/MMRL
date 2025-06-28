package com.dergoogler.mmrl.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.platform.Platform.Companion.putPlatform
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.putModId
import com.dergoogler.mmrl.platform.model.ModuleConfig.Companion.asModuleConfig
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.topjohnwu.superuser.Shell

val Float.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }

val Long.toFormattedDateSafely: String
    @Composable
    get() {
        val prefs = LocalUserPreferences.current
        return this.toFormattedDateSafely(prefs.datePattern)
    }


inline fun <T> withNewRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
    block: Shell.() -> T,
): T {
    return createRootShell(globalMnt, debug, commands).use(block)
}

fun createRootShell(
    globalMnt: Boolean = false,
    debug: Boolean = false,
    commands: Array<String> = arrayOf("su"),
): Shell {
    Shell.enableVerboseLogging = debug
    val builder = Shell.Builder.create()
    if (globalMnt) {
        builder.setFlags(Shell.FLAG_MOUNT_MASTER)
    }
    return builder.build(*commands)
}

internal val WebUIXPackageName = "com.dergoogler.mmrl.wx${if (BuildConfig.DEBUG) ".debug" else ""}"

fun UserPreferences.launchWebUI(context: Context, modId: ModId) {
    val config = modId.asModuleConfig

    val launchWX: (ModId) -> Unit = { modId ->
        val intent = Intent().apply {

            component = ComponentName(
                WebUIXPackageName,
                "com.dergoogler.mmrl.wx.ui.activity.webui.WebUIActivity"
            )
            putModId(modId)
            putPlatform(workingMode.toPlatform())
        }

        context.startActivity(intent)
    }

    val launchWL: (ModId) -> Unit = { modId ->
        val intent = Intent().apply {
            component = ComponentName(
                WebUIXPackageName,
                "com.dergoogler.mmrl.wx.ui.activity.webui.KsuWebUIActivity"
            )
            putModId(modId)
            putPlatform(workingMode.toPlatform())
        }

        context.startActivity(intent)
    }

    if (webuiEngine == WebUIEngine.PREFER_MODULE) {
        val configEngine = config.getWebuiEngine(context)

        if (configEngine == null) {
            launchWX(modId)
            return
        }

        when (configEngine) {
            "wx" -> launchWX(modId)
            "ksu" -> launchWL(modId)
            else -> Toast.makeText(context, "Unknown WebUI engine", Toast.LENGTH_SHORT).show()
        }

        return
    }


    if (webuiEngine == WebUIEngine.WX) {
        launchWX(modId)
        return

    }

    if (webuiEngine == WebUIEngine.KSU) {
        launchWL(modId)
        return
    }

    Toast.makeText(context, "Unknown WebUI engine", Toast.LENGTH_SHORT).show()
}