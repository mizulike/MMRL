package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import com.dergoogler.mmrl.platform.Compat
import com.dergoogler.mmrl.model.local.ModuleAnalytics
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.dergoogler.mmrl.platform.content.NullableBoolean
import dev.dergoogler.mmrl.compat.viewmodel.MMRLViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    val isProviderAlive get() = Compat.isAlive
    val platform get() = Compat.platform

    val versionName: String
        get() = Compat.get("") {
            with(moduleManager) { version }
        }

    val isLkmMode: NullableBoolean
        get() = Compat.get(NullableBoolean(null)) {
            with(moduleManager) { isLkmMode }
        }

    val versionCode
        get() = Compat.get(0) {
            with(moduleManager) { versionCode }
        }

    val seLinuxContext: String
        get() = Compat.get("Failed") {
            with(moduleManager) {
                seLinuxContext
            }
        }

    val superUserCount: Int
        get() = Compat.get(-1) {
            with(moduleManager) {
                superUserCount
            }
        }

    fun analytics(context: Context): ModuleAnalytics? = Compat.get(null) {
        with(moduleManager) {
            val local = runBlocking { localRepository.getLocalAllAsFlow().first() }
            return@get ModuleAnalytics(
                context = context,
                local = local
            )
        }
    }

    init {
        Timber.d("HomeViewModel init")
    }

    fun reboot(reason: String = "") {
        Compat.get(Unit) {
            with(moduleManager) {
                reboot(reason)
            }
        }
    }
}