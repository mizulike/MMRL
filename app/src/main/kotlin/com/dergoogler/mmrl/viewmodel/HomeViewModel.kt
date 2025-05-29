package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import com.dergoogler.mmrl.model.local.ModuleAnalytics
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.file.SuFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    val isProviderAlive get() = Platform.isAlive
    val platform get() = Platform.platform

    val versionName: String
        get() = Platform.get("") {
            with(moduleManager) { version }
        }

    val isLkmMode: NullableBoolean
        get() = Platform.get(NullableBoolean(null)) {
            with(moduleManager) { isLkmMode }
        }

    val versionCode
        get() = Platform.get(0) {
            with(moduleManager) { versionCode }
        }

    val seLinuxContext: String
        get() = Platform.get("Failed") {
            seLinuxContext
        }

    val superUserCount: Int
        get() = Platform.get(-1) {
            with(moduleManager) {
                superUserCount
            }
        }

    private suspend fun getLocalModule() = withContext(Dispatchers.IO) {
        localRepository.getLocalAllAsFlow().first()
    }

    private suspend fun getTotalModules() = getLocalModule().size

    private suspend fun getTotalByState(state: State): Int {
        val local = getLocalModule()
        return local.filter { it.state == state }.size
    }

    private suspend fun getTotalEnabled(): Int = getTotalByState(State.ENABLE)
    private suspend fun getTotalDisabled(): Int = getTotalByState(State.DISABLE)
    private suspend fun getTotalUpdated(): Int = getTotalByState(State.UPDATE)

    private val modulesImg = "/data/adb/ksu/modules.img"

    private suspend fun getTotalModulesUsageBytes() =
        SuFile("/data/adb/modules").lengthAsync(recursive = true, skipPaths = listOf(modulesImg))

    private val totalDeviceStorageBytes: Long
        get() {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            return try {
                val storageStatsManager =
                    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
                val totalBytes = storageStatsManager.getTotalBytes(uuid)
                totalBytes
            } catch (e: IOException) {
                Timber.d("ModuleAnalytics>totalDeviceStorageBytes: $e")
                0L
            }
        }

    suspend fun getAnalytics(): ModuleAnalytics {
        val totalModulesUsageBytes = getTotalModulesUsageBytes()

        val totalStorageUsage =
            totalModulesUsageBytes.toFloat() / totalDeviceStorageBytes.toFloat()

        return ModuleAnalytics(
            totalModules = getTotalModules(),
            totalEnabled = getTotalEnabled(),
            totalDisabled = getTotalDisabled(),
            totalUpdated = getTotalUpdated(),
            totalModulesUsage = totalModulesUsageBytes,
            totalDeviceStorage = totalDeviceStorageBytes,
            totalStorageUsage = totalStorageUsage.toFloat(),
        )
    }

    init {
        Timber.d("HomeViewModel init")
    }

    fun reboot(reason: String = "") {
        Platform.get(Unit) {
            with(moduleManager) {
                reboot(reason)
            }
        }
    }
}