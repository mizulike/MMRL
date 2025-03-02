package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.datastore.superuser.SuperUserMenuCompat
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.dergoogler.mmrl.compat.content.AppInfo
import dev.dergoogler.mmrl.compat.content.AppProfile
import dev.dergoogler.mmrl.compat.viewmodel.MMRLViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rikka.parcelablelist.ParcelableListSlice
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SuperUserViewModel @Inject constructor(
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
    application: Application,
) : MMRLViewModel(
    application = application,
    localRepository = localRepository,
    modulesRepository = modulesRepository,
    userPreferencesRepository = userPreferencesRepository
) {
    private val superUserMenu
        get() = userPreferencesRepository.data
            .map { it.superUserMenu }

    var isSearch by mutableStateOf(false)
        private set
    private val keyFlow = MutableStateFlow("")
    val query get() = keyFlow.asStateFlow()

    fun getAppProfile(key: String?, uid: Int) = Compat.get(null) {
        with(moduleManager) {
            getAppProfile(key, uid)
        }
    }

    fun setAppProfile(profile: AppProfile?) = Compat.get(false) {
        with(moduleManager) {
            setAppProfile(profile)
        }
    }

    fun uidShouldUmount(uid: Int) = Compat.get(false) {
        with(moduleManager) {
            uidShouldUmount(uid)
        }
    }

    private val packagesFlow: Flow<ParcelableListSlice<PackageInfo>> =
        MutableStateFlow(Compat.ksuService.getPackages(0))

    fun fetch() = viewModelScope.launch {
        refreshing {
            dataObserver()
            keyObserver()
        }
    }

    private val cacheFlow = MutableStateFlow(listOf<AppInfo>())
    private val appsFlow = MutableStateFlow(listOf<AppInfo>())
    val apps get() = appsFlow.asStateFlow()

    init {
        dataObserver()
        keyObserver()
    }

    var isLoading by mutableStateOf(true)
        private set
    private var progressFlow = MutableStateFlow(false)
    val progress get() = progressFlow.asStateFlow()

    private inline fun <T> T.refreshing(callback: T.() -> Unit) {
        progressFlow.update { true }
        callback()
        progressFlow.update { false }
    }

    val screenState: StateFlow<ModulesScreenState> = localRepository.getLocalAllAsFlow()
        .combine(progress) { items, isRefreshing ->
            ModulesScreenState(items = items, isRefreshing = isRefreshing)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModulesScreenState()
        )

    fun search(key: String) {
        keyFlow.value = key
    }

    fun openSearch() {
        isSearch = true
    }

    fun closeSearch() {
        isSearch = false
        keyFlow.value = ""
    }

    private fun keyObserver() {
        combine(
            keyFlow,
            cacheFlow
        ) { key, source ->
            val newKey = when {
                key.startsWith("pkg:", ignoreCase = true) -> key.removePrefix("pkg:")
                else -> key
            }.trim()

            appsFlow.value = source.filter { m ->
                if (key.isNotBlank() || newKey.isNotBlank()) {
                    when {
                        key.startsWith("pkg:", ignoreCase = true) ->
                            m.packageName.equals(newKey, ignoreCase = true)

                        else ->
                            m.packageName.contains(key, ignoreCase = true) ||
                                    m.label.contains(key, ignoreCase = true)
                    }
                } else {
                    true
                }
            }
        }.launchIn(viewModelScope)
    }


    private fun dataObserver() {
        combine(
            packagesFlow,
            superUserMenu
        ) { packages, menu ->
            cacheFlow.value = packages.list.map {
                val appInfo = it.applicationInfo
                val uid = appInfo!!.uid
                val profile = getAppProfile(it.packageName, uid)
                AppInfo(
                    packageName = it.packageName,
                    profile = profile,
                )
            }
                .sortedWith(comparator(menu.descending))
                .filter {
                    it.uid == 2000
                            || menu.showSystemApps || it.packageInfo.applicationInfo!!.flags.and(
                        ApplicationInfo.FLAG_SYSTEM
                    ) == 0
                }
                .filter { it.packageName != context.packageName }

            isLoading = false

        }.launchIn(viewModelScope)
    }

    private fun comparator(descending: Boolean): Comparator<AppInfo> {
        val comparable = if (descending) {
            compareByDescending<AppInfo> {
                when {
                    it.allowSu -> 0
                    it.hasCustomProfile -> 1
                    else -> 2
                }
            }
        } else {
            compareBy {
                when {
                    it.allowSu -> 0
                    it.hasCustomProfile -> 1
                    else -> 2
                }
            }
        }

        return comparable.then(compareBy(Collator.getInstance(Locale.getDefault()), AppInfo::label))
    }

    fun setSuperUserMenu(value: SuperUserMenuCompat) {
        viewModelScope.launch {
            userPreferencesRepository.setSuperUserMenu(value)
        }
    }
}