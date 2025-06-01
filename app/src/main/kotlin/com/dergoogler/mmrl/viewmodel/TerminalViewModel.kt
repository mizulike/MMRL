package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const.CLEAR_CMD
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.ui.activity.terminal.Actions
import com.dergoogler.mmrl.ui.activity.terminal.ShellBroadcastReceiver
import com.dergoogler.mmrl.utils.createRootShell
import dev.dergoogler.mmrl.compat.BuildCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

open class TerminalViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    internal val logs = mutableListOf<String>()
    internal val console = mutableStateListOf<String>()
    var event by mutableStateOf(Event.LOADING)
        internal set
    var shell by mutableStateOf(createRootShell())
        internal set

    private val localFlow = MutableStateFlow<LocalModule?>(null)
    val local get() = localFlow.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    fun registerReceiver() {
        if (receiver == null) {
            receiver = ShellBroadcastReceiver(context, console, logs)

            val filter = IntentFilter().apply {
                addAction(Actions.SET_LAST_LINE)
                addAction(Actions.REMOVE_LAST_LINE)
                addAction(Actions.CLEAR_TERMINAL)
                addAction(Actions.LOG)
            }

            if (BuildCompat.atLeastT) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
        }
    }

    fun unregisterReceiver() {
        if (receiver == null) {
            Timber.w("ShellBroadcastReceiver is already null")
            return
        }

        context.unregisterReceiver(receiver)
        receiver = null
    }

    override fun onCleared() {
        shell.close()
        super.onCleared()
    }

    private fun IntentFilter.addAction(action: Actions) {
        addAction("${context.packageName}.${action.name}")
    }

    val moduleManager: IModuleManager? = PlatformManager.get(null) {
            this.moduleManager
        }

    val fileManager: IFileManager? = PlatformManager.get(null) {
            this.fileManager
        }

    fun reboot(reason: String = "") {
        if (moduleManager == null) {
            Toast.makeText(
                context,
                context.getString(R.string.unable_to_perform_reboot_task), Toast.LENGTH_SHORT
            ).show()
            return
        }

        moduleManager.reboot(reason)
    }

    suspend fun writeLogsTo(uri: Uri) = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(logs.joinToString(separator = "\n").toByteArray())
            }
        }.onFailure {
            Timber.e(it)
        }
    }

    private val localizedEnglishResources
        get(): Resources {
            var conf: Configuration = context.resources.configuration
            conf = Configuration(conf)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = context.createConfigurationContext(conf)
            return localizedContext.resources
        }

    private val devMode = runBlocking { userPreferencesRepository.data.first().developerMode }

    internal fun devLog(@StringRes message: Int, vararg format: Any?) {
        if (devMode) log(message, *format)
    }

    internal fun devLog(@StringRes message: Int) {
        if (devMode) log(message)
    }

    internal fun log(@StringRes message: Int, vararg format: Any?) {
        log(
            message = context.getString(message, *format),
            log = localizedEnglishResources.getString(message, *format)
        )
    }

    internal fun log(@StringRes message: Int) {
        log(
            message = context.getString(message),
            log = localizedEnglishResources.getString(message)
        )
    }

    internal fun log(
        message: String,
        log: String = message,
    ) {
        if (message.startsWith(CLEAR_CMD)) {
            console.clear()
        } else {
            console.add(message)
            logs.add(log)
        }
    }
}
