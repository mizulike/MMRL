package com.dergoogler.mmrl.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModuleConfig.Companion.asModuleConfig
import com.dergoogler.mmrl.ui.activity.webui.KsuWebUIActivity
import com.dergoogler.mmrl.ui.activity.webui.WebUIActivity
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.webui.activity.WXActivity.Companion.launchWebUI
import com.dergoogler.mmrl.webui.activity.WXActivity.Companion.launchWebUIX
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

fun UserPreferences.launchWebUI(context: Context, modId: ModId) {
    val config = modId.asModuleConfig

    if (webuiEngine == WebUIEngine.PREFER_MODULE) {
        val configEngine = config.getWebuiEngine(context)

        if (configEngine == null) {
            context.launchWebUIX<WebUIActivity>(modId)
            return
        }

        when (configEngine) {
            "wx" -> context.launchWebUIX<WebUIActivity>(modId)
            "ksu" -> context.launchWebUI<KsuWebUIActivity>(modId.id)
            else -> Toast.makeText(context, "Unknown WebUI engine", Toast.LENGTH_SHORT).show()
        }

        return
    }


    if (webuiEngine == WebUIEngine.WX) {
        context.launchWebUIX<WebUIActivity>(modId)
        return

    }

    if (webuiEngine == WebUIEngine.KSU) {
        context.launchWebUI<KsuWebUIActivity>(modId.id)
        return
    }

    Toast.makeText(context, "Unknown WebUI engine", Toast.LENGTH_SHORT).show()
}