package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const.CLEAR_CMD
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.AlertType
import com.dergoogler.mmrl.model.terminal.Block
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.utils.createRootShell
import com.dergoogler.mmrl.utils.initPlatform
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    protected val logs = mutableListOf<String>()

    /**
     * Instead of plain string lines, console holds parsed blocks,
     * updated incrementally on every log call.
     */
    val console = mutableStateListOf<Block>()

    var event by mutableStateOf(Event.LOADING)
        protected set

    var shell by mutableStateOf(createRootShell())
        protected set

    private val localFlow = MutableStateFlow<LocalModule?>(null)
    val local get() = localFlow.asStateFlow()

    private val commandSet =
        setOf("group", "endgroup", "card", "endcard", "add-mask", "error", "warning", "notice")

    private var currentGroup: GroupBlock? = null
    private var currentCard: CardBlock? = null
    private var lineNumber = 1

    private val masks = mutableListOf<String>()

    protected val platformReadyDeferred = CompletableDeferred<Boolean>()

    init {
        viewModelScope.launch {
            val userPreferences = userPreferencesRepository.data.first()

            devLog(R.string.waiting_for_platformmanager_to_initialize)

            val deferred = initPlatform(
                scope = viewModelScope,
                context = context,
                platform = userPreferences.workingMode.toPlatform()
            )

            val platformInitializedSuccessfully = deferred.await()
            if (!platformInitializedSuccessfully) {
                event = Event.FAILED
                log(R.string.failed_to_initialize_platform)
                platformReadyDeferred.complete(false)
            } else {
                devLog(R.string.platform_initialized)
                platformReadyDeferred.complete(true)
            }
        }
    }

    override fun onCleared() {
        shell.close()
        currentCard = null
        currentGroup = null
        super.onCleared()
    }

    fun reboot(reason: String = "") {
        PlatformManager.moduleManager.reboot(reason)
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

    @MainThread
    protected fun devLog(message: String) {
        if (devMode) log(message)
    }

    @MainThread
    protected fun devLog(@StringRes message: Int, vararg format: Any?) {
        if (devMode) log(message, *format)
    }

    @MainThread
    protected fun devLog(@StringRes message: Int) {
        if (devMode) log(message)
    }

    @MainThread
    protected fun log(@StringRes message: Int, vararg format: Any?) {
        log(
            message = context.getString(message, *format),
            log = localizedEnglishResources.getString(message, *format)
        )
    }

    @MainThread
    protected fun log(@StringRes message: Int) {
        log(
            message = context.getString(message),
            log = localizedEnglishResources.getString(message)
        )
    }

    @MainThread
    protected fun log(
        message: String,
        log: String = message,
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            if (message.startsWith(CLEAR_CMD)) {
                console.clear()
                logs.clear()
                lineNumber = 1
                currentGroup = null
                currentCard = null
                masks.clear()
            } else {
                val command = ActionCommand.tryParseV2(message, commandSet)

                when (command?.command) {
                    "group" -> {
                        currentGroup = GroupBlock(
                            title = command.data.ifBlank { command.properties["title"] },
                            startLine = lineNumber,
                            initiallyExpanded = false
                        )
                    }

                    "endgroup" -> {
                        currentGroup?.let { console += it }
                        currentGroup = null
                    }

                    "card" -> {
                        currentCard = CardBlock()
                    }

                    "endcard" -> {
                        currentCard?.let { console += it }
                        currentCard = null
                    }

                    "add-mask" -> {
                        command.data.takeIf { it.isNotBlank() }?.let {
                            masks += it
                            console += TextBlock(
                                lineNumber = lineNumber,
                                text = applyMasks(it)
                            )
                        }
                    }

                    "error" -> {
                        command.data.takeIf { it.isNotBlank() }?.let {
                            console += AlertBlock(
                                lineNumber = lineNumber,
                                type = AlertType.ERROR,
                                text = it
                            )
                        }
                    }

                    "warning" -> {
                        command.data.takeIf { it.isNotBlank() }?.let {
                            console += AlertBlock(
                                lineNumber = lineNumber,
                                type = AlertType.WARNING,
                                text = it
                            )
                        }
                    }

                    "notice" -> {
                        command.data.takeIf { it.isNotBlank() }?.let {
                            console += AlertBlock(
                                lineNumber = lineNumber,
                                type = AlertType.NOTICE,
                                text = it
                            )
                        }
                    }

                    else -> {
                        val entry = lineNumber to message
                        when {
                            currentGroup != null -> currentGroup?.lines?.add(entry)
                            currentCard != null -> currentCard?.lines?.add(entry)
                            else -> console += TextBlock(
                                lineNumber = lineNumber,
                                text = message
                            )
                        }
                    }
                }

                logs += log
                lineNumber++
            }
        }
    }

    private fun applyMasks(line: String): String {
        var maskedLine = line
        for (mask in masks) {
            if (mask.isNotEmpty()) {
                maskedLine = maskedLine.replace(mask, "••••••••")
            }
        }
        return maskedLine
    }
}
