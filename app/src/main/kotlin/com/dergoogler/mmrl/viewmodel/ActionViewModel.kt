package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.dergoogler.mmrl.platform.content.State
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.utils.initPlatform
import com.topjohnwu.superuser.CallbackList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ActionViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : TerminalViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    val logfile get() = "Action_${LocalDateTime.now()}.log"

    init {
        Timber.d("ActionViewModel initialized")
    }

    suspend fun runAction(scope: CoroutineScope, modId: ModId) {
        val module = localModule(modId.toString())
        val userPreferences = userPreferencesRepository.data.first()
        event = Event.LOADING

        devLog(R.string.waiting_for_platformmanager_to_initialize)

        val deferred = initPlatform(
            scope = scope,
            context = context,
            platform = userPreferences.workingMode.toPlatform()
        )

        if (!deferred.await()) {
            event = Event.FAILED
            log(R.string.failed_to_initialize_platform)
            return
        } else {
            devLog(R.string.platform_initialized)
        }

        if (moduleManager == null) {
            event = Event.FAILED
            log(R.string.module_manager_is_null)
            return
        }

        if (fileManager == null) {
            event = Event.FAILED
            log(R.string.file_manager_is_null)
            return
        }

        if (module == null) {
            event = Event.FAILED
            log(R.string.module_not_found)
            return
        }

        if (!module.hasAction) {
            event = Event.FAILED
            log(R.string.this_module_don_t_have_an_action)
            return
        }

        if (module.state == State.DISABLE || module.state == State.REMOVE) {
            event = Event.FAILED
            log(R.string.module_is_disabled_or_removed_unable_to_execute_action)
            return
        }

        val result = action(modId, userPreferences.useShellForModuleAction)

        event = if (result) {
            Event.SUCCEEDED
        } else {
            Event.FAILED
        }
    }

    private suspend fun action(modId: ModId, legacy: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            val actionResult = CompletableDeferred<Boolean>()

            val stdout = object : CallbackList<String?>() {
                override fun onAddElement(msg: String?) {
                    if (msg == null) return

                    viewModelScope.launch {
                        log(msg)
                    }
                }
            }

            val stderr = object : CallbackList<String?>() {
                override fun onAddElement(msg: String?) {
                    if (msg == null) return

                    viewModelScope.launch {
                        logs.add(msg)
                    }
                }
            }

            val cmds = if (legacy || platform.isMagisk) {
                listOf(
                    "export PATH=/data/adb/ap/bin:/data/adb/ksu/bin:/data/adb/magisk:\$PATH",
                    "export MMRL=true",
                    "export MMRL_VER=${BuildConfig.VERSION_NAME}",
                    "export MMRL_VER_CODE=${BuildConfig.VERSION_CODE}",
                    "export BOOTMODE=true",
                    "export ARCH=${Build.SUPPORTED_ABIS[0]}",
                    "export API=${Build.VERSION.SDK_INT}",
                    "export IS64BIT=${Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()}",
                    "busybox sh /data/adb/modules/$modId/action.sh"
                )
            } else {
                listOf(moduleManager!!.getActionCommand(modId))
            }

            val result = shell.newJob().add(*cmds.toTypedArray()).to(stdout, stderr).exec()

            if (result.isSuccess) {
                actionResult.complete(true)
            } else {
                log(R.string.execution_failed_try_to_use_shell_for_the_action_execution_settings_module_use_shell_for_module_action)
                actionResult.complete(false)
            }

            return@withContext actionResult.await()
        }

}
