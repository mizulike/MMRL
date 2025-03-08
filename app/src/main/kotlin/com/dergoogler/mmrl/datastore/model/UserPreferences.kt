package com.dergoogler.mmrl.datastore.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ui.theme.Colors
import dev.dergoogler.mmrl.compat.BuildCompat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.InputStream
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class UserPreferences @OptIn(ExperimentalSerializationApi::class) constructor(
    val workingMode: WorkingMode = WorkingMode.FIRST_SETUP,
    val darkMode: DarkMode = DarkMode.FollowSystem,
    val themeColor: Int = if (BuildCompat.atLeastS) Colors.Dynamic.id else Colors.MMRLBase.id,
    val deleteZipFile: Boolean = false,
    val downloadPath: String = Const.PUBLIC_DOWNLOADS.path,
    val homepage: Homepage = Homepage.Home,
    @ProtoNumber(20)
    val repositoryMenu: RepositoryMenu = RepositoryMenu(),
    @ProtoNumber(30)
    val modulesMenu: ModulesMenu = ModulesMenu(),
    @ProtoNumber(40)
    val repositoriesMenu: RepositoriesMenu = RepositoriesMenu(),
    val useDoh: Boolean = false,
    val confirmReboot: Boolean = true,
    val terminalTextWrap: Boolean = false,
    val datePattern: String = "d MMMM yyyy",
    val autoUpdateRepos: Boolean = false,
    val autoUpdateReposInterval: Int = 0,
    val checkModuleUpdates: Boolean = false,
    val checkModuleUpdatesInterval: Int = 0,
    val checkAppUpdates: Boolean = true,
    val checkAppUpdatesPreReleases: Boolean = false,
    val hideFingerprintInHome: Boolean = true,
    val webUiDevUrl: String = "https://127.0.0.1:8080",
    val developerMode: Boolean = false,
    val useWebUiDevUrl: Boolean = false,
    val useShellForModuleStateChange: Boolean = false,
    val useShellForModuleAction: Boolean = true,
    val clearInstallTerminal: Boolean = true,
    val allowCancelInstall: Boolean = false,
    val allowCancelAction: Boolean = false,
    val blacklistAlerts: Boolean = true,
    val injectEruda: List<String> = emptyList(),
    val allowedFsModules: List<String> = emptyList(),
    val allowedKsuModules: List<String> = emptyList(),
    val useProviderAsBackgroundService: Boolean = false,
) {
    @Composable
    fun isDarkMode() = when (darkMode) {
        DarkMode.AlwaysOff -> false
        DarkMode.AlwaysOn -> true
        DarkMode.FollowSystem -> isSystemInDarkTheme()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun encodeTo(output: OutputStream) = output.write(
        ProtoBuf.encodeToByteArray(this)
    )

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun decodeFrom(input: InputStream): UserPreferences =
            ProtoBuf.decodeFromByteArray(input.readBytes())
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(block: UserPreferences.() -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode) block() else null
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean,
    block: UserPreferences.() -> R,
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode && also()) block() else null
}

@OptIn(ExperimentalContracts::class)
inline fun UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean
): Boolean {
    contract {
        callsInPlace(also, InvocationKind.AT_MOST_ONCE)
    }

    return developerMode && also()
}

@OptIn(ExperimentalContracts::class)
inline fun <R> UserPreferences.developerMode(
    also: UserPreferences.() -> Boolean,
    default: R,
    block: UserPreferences.() -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (developerMode && also()) block() else default
}