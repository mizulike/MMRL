package com.dergoogler.mmrl.ui.screens.superuser.screens.appProfile


import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dergoogler.mmrl.Compat
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.NavigateUpTopBar
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.utils.none
import dev.dergoogler.mmrl.compat.content.AppInfo
import dev.dergoogler.mmrl.compat.content.AppProfile
import kotlinx.coroutines.launch

object Profile {
    fun get(key: String?, uid: Int) = Compat.get(null) {
        with(moduleManager) {
            getAppProfile(key, uid)
        }
    }

    fun set(profile: AppProfile?) = Compat.get(false) {
        with(moduleManager) {
            setAppProfile(profile)
        }
    }
}

@Composable
fun AppProfileScreen(app: AppInfo) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val failToUpdateAppProfile =
        stringResource(R.string.failed_to_update_app_profile).format(app.label)
    val failToUpdateSepolicy = stringResource(R.string.failed_to_update_sepolicy).format(app.label)
    val suNotAllowed = stringResource(R.string.su_not_allowed).format(app.label)

    val scope = rememberCoroutineScope()

    val initialProfile = Profile.get(app.packageName, app.uid)!!
    if (initialProfile.allowSu) {
        initialProfile.rules = ""
    }
    var profile by rememberSaveable {
        mutableStateOf(initialProfile)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NavigateUpTopBar(
                title = stringResource(id = R.string.app_profile),
                scrollBehavior = scrollBehavior,
                navController = navController,
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        AppProfileInner(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            packageName = app.packageName,
            appLabel = app.label,
            appIcon = {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(app.packageInfo).crossfade(true)
                        .build(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .padding(4.dp)
                        .width(48.dp)
                        .height(48.dp)
                )
            },
            profile = profile,
            onProfileChange = {
                scope.launch {
                    if (it.allowSu) {
                        // sync with allowlist.c - forbid_system_uid
                        if (app.uid < 2000 && app.uid != 1000) {
                            snackBarHost.showSnackbar(suNotAllowed)
                            return@launch
                        }
                        if (!it.rootUseDefault && it.rules.isNotEmpty() /*&& !setSepolicy(profile.name, it.rules)*/) {
                            snackBarHost.showSnackbar(failToUpdateSepolicy)
                            return@launch
                        }
                    }
                    if (!Profile.set(it)) {
                        snackBarHost.showSnackbar(failToUpdateAppProfile.format(app.uid))
                    } else {
                        profile = it
                    }
                }
            },
        )
    }
}

@Composable
private fun AppProfileInner(
    modifier: Modifier = Modifier,
    packageName: String,
    appLabel: String,
    appIcon: @Composable () -> Unit,
    profile: AppProfile,
    onProfileChange: (AppProfile) -> Unit,
) {
    val isRootGranted = profile.allowSu

    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(appLabel) },
            supportingContent = { Text(packageName) },
            leadingContent = appIcon,
        )


        ListSwitchItem(
            title = stringResource(id = R.string.page_superuser),
            checked = isRootGranted,
            onChange = { onProfileChange(profile.copy(allowSu = it)) },
        )

        Crossfade(targetState = isRootGranted, label = "") { current: Boolean ->
            current
            Column(
                modifier = Modifier.padding(bottom = 6.dp + 48.dp + 6.dp /* SnackBar height */)
            ) {
                if (current) {
                    val initialMode = if (profile.rootUseDefault) {
                        Mode.Default
                    } else {
                        Mode.Custom
                    }
                    var mode by rememberSaveable {
                        mutableStateOf(initialMode)
                    }
                    ProfileBox(mode, true) {
                        // template mode shouldn't change profile here!
                        if (it == Mode.Default || it == Mode.Custom) {
                            onProfileChange(profile.copy(rootUseDefault = it == Mode.Default))
                        }
                        mode = it
                    }
                    Crossfade(targetState = mode, label = "") { currentMode ->
                        currentMode
                        if (mode == Mode.Custom) {
                            Text(text = "NULL")
//                            RootProfileConfig(
//                                fixedName = true,
//                                profile = profile,
//                                onProfileChange = onProfileChange
//                            )
                        }
                    }
                } else {
                    val mode = if (profile.nonRootUseDefault) Mode.Default else Mode.Custom
                    ProfileBox(mode, false) {
                        onProfileChange(profile.copy(nonRootUseDefault = (it == Mode.Default)))
                    }
                    Crossfade(targetState = mode, label = "") { currentMode ->
                        val modifyEnabled = currentMode == Mode.Custom
                        Text(text = "NULL")
//                        AppProfileConfig(
//                            fixedName = true,
//                            profile = profile,
//                            enabled = modifyEnabled,
//                            onProfileChange = onProfileChange
//                        )
                    }
                }
            }
        }
    }
}

private enum class Mode(@StringRes private val res: Int) {
    Default(R.string.profile_default), Custom(R.string.profile_custom);

    val text: String
        @Composable get() = stringResource(res)
}

@Composable
private fun ProfileBox(
    mode: Mode,
    hasTemplate: Boolean,
    onModeChange: (Mode) -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.app_profile)) },
        supportingContent = { Text(mode.text) },
        leadingContent = { Icon(Icons.Filled.AccountCircle, null) },
    )
    HorizontalDivider(thickness = Dp.Hairline)
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = mode == Mode.Default,
                    label = { Text(stringResource(R.string.profile_default)) },
                    onClick = { onModeChange(Mode.Default) },
                )
                FilterChip(
                    selected = mode == Mode.Custom,
                    label = { Text(stringResource(R.string.profile_custom)) },
                    onClick = { onModeChange(Mode.Custom) },
                )
            }
        }
    )
}