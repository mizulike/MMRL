package com.dergoogler.mmrl.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ext.navigateSingleTopTo
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.model.local.managers
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.TopAppBarTitle
import com.dergoogler.mmrl.ui.component.WorkingModeBottomSheet
import com.dergoogler.mmrl.ui.component.dialog.ConfirmData
import com.dergoogler.mmrl.ui.component.dialog.rememberConfirm
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.ListRadioCheckItem
import com.dergoogler.mmrl.ui.navigation.graphs.SettingsScreen
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.jakewharton.processphoenix.ProcessPhoenix

@Composable
fun SettingsScreen() {
    val userPreferences = LocalUserPreferences.current
    val viewModel = LocalSettings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val navController = LocalNavController.current
    val browser = LocalUriHandler.current
    val context = LocalContext.current
    val confirm = rememberConfirm(context)

    var workingModeBottomSheet by remember { mutableStateOf(false) }
    if (workingModeBottomSheet) WorkingModeBottomSheet(
        onClose = {
            workingModeBottomSheet = false
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            ListButtonItem(
                icon = R.drawable.color_swatch,
                title = stringResource(id = R.string.settings_appearance),
                desc = stringResource(id = R.string.settings_appearance_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Appearance.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.shield,
                title = stringResource(id = R.string.settings_security),
                desc = stringResource(id = R.string.settings_security_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Security.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.refresh,
                title = stringResource(id = R.string.settings_updates),
                desc = stringResource(id = R.string.settings_updates_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Updates.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.stack_middle,
                enabled = viewModel.isProviderAlive,
                title = stringResource(id = R.string.settings_modules),
                desc = stringResource(id = R.string.settings_modules_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Modules.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.tool,
                title = stringResource(id = R.string.settings_other),
                desc = stringResource(id = R.string.settings_other_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Other.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.file_3d,
                title = stringResource(id = R.string.settings_resources),
                desc = stringResource(id = R.string.settings_resources_desc),
                onClick = {
                    browser.openUri(Const.RESOURCES_URL)
                }
            )

            val manager = managers.find { userPreferences.workingMode == it.platform }

            manager.nullable { mng ->
                ListRadioCheckItem(
                    icon = mng.icon,
                    title = stringResource(id = R.string.platform),
                    desc = stringResource(mng.name),
                    options = managers.map { it.toRadioOption() },
                    onConfirm = {
                        confirm(
                            ConfirmData(
                                title = context.getString(R.string.change_platform),
                                description = context.getString(R.string.working_mode_change_dialog_desc),
                                closeText = context.getString(R.string.keep),
                                onClose = {},
                                confirmText = context.getString(R.string.apply),
                                onConfirm = {
                                    viewModel.setWorkingMode(it.value)
                                    ProcessPhoenix.triggerRebirth(context)
                                }
                            )
                        )
                    },
                    value = mng.platform
                )
            }

            ListButtonItem(
                icon = R.drawable.files,
                title = stringResource(id = R.string.settings_changelog),
                desc = stringResource(id = R.string.settings_changelo_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Changelog.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.file_shredder,
                title = stringResource(id = R.string.settings_blacklist),
                desc = stringResource(id = R.string.settings_blacklist_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Blacklist.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.logs,
                title = stringResource(id = R.string.settings_log_viewer),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.LogViewer.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.bug,
                title = stringResource(id = R.string.settings_developer),
                desc = stringResource(id = R.string.settings_developer_desc),
                onClick = {
                    navController.navigateSingleTopTo(SettingsScreen.Developer.route)
                }
            )

            ListButtonItem(
                icon = R.drawable.spy,
                title = stringResource(id = R.string.settings_privacy_policy),
                onClick = {
                    browser.openUri(Const.PRIVACY_POLICY_URL)
                }
            )

            ListButtonItem(
                icon = R.drawable.files,
                title = stringResource(id = R.string.settings_terms_of_service),
                onClick = {
                    browser.openUri(Const.TERMS_OF_SERVICE_URL)
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
) = TopAppBar(
    title = {
        TopAppBarTitle(text = stringResource(id = R.string.page_settings))
    },
    scrollBehavior = scrollBehavior
)