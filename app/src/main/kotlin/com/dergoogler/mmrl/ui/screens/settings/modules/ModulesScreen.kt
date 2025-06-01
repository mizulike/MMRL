package com.dergoogler.mmrl.ui.screens.settings.modules

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.Homepage
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.ui.component.APatchLabel
import com.dergoogler.mmrl.ui.component.KernelSuLabel
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.dialog.RadioOptionItem
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.component.listItem.ListRadioCheckItem
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun ModulesScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_modules,
    ) {
        ListHeader(
            title = stringResource(id = R.string.settings_modules_handlers)
        )

        ListSwitchItem(
            enabled = viewModel.isProviderAlive && viewModel.platform.isNotMagisk,
            title = stringResource(id = R.string.settings_shell_module_state_change),
            desc = stringResource(id = R.string.settings_shell_module_state_change_desc),
            checked = userPreferences.useShellForModuleStateChange && viewModel.platform.isNotMagisk,
            onChange = viewModel::setUseShellForModuleStateChange,
            base = {
                labels = listOf { KernelSuLabel(); APatchLabel() }
            }
        )

        ListSwitchItem(
            enabled = viewModel.isProviderAlive && viewModel.platform.isNotMagisk,
            title = stringResource(id = R.string.settings_use_generic_action),
            desc = stringResource(id = R.string.settings_use_generic_action_desc),
            checked = userPreferences.useShellForModuleAction,
            onChange = viewModel::setUseShellForModuleAction,
            base = {
                labels = listOf { KernelSuLabel(); APatchLabel() }
            }
        )

        ListHeader(
            title = stringResource(id = R.string.settings_modules_installer)
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_clear_install_terminal),
            desc = stringResource(id = R.string.settings_clear_install_terminal_desc),
            checked = userPreferences.clearInstallTerminal,
            onChange = viewModel::setClearInstallTerminal,
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_delete_zip),
            desc = stringResource(id = R.string.settings_delete_zip_desc),
            checked = userPreferences.deleteZipFile,
            onChange = viewModel::setDeleteZipFile,
            enabled = userPreferences.workingMode.isRoot
        )

        ListSwitchItem(
            title = stringResource(R.string.allow_cancel_installation),
            checked = userPreferences.allowCancelInstall,
            onChange = viewModel::setAllowCancelInstall,
        )

        ListHeader(
            title = stringResource(id = R.string.action_activity)
        )

        ListSwitchItem(
            title = stringResource(R.string.allow_cancel_action),
            checked = userPreferences.allowCancelAction,
            onChange = viewModel::setAllowCancelAction,
        )

        ListHeader(
            title = stringResource(id = R.string.view_module_features_webui)
        )

        ListRadioCheckItem(
            enabled = viewModel.isProviderAlive,
            title = stringResource(R.string.settings_webui_engine),
            desc = stringResource(R.string.settings_webui_engine_desc),
            value = userPreferences.webuiEngine,
            options = listOf(
                RadioOptionItem(
                    value = WebUIEngine.WX,
                    title = stringResource(R.string.settings_webui_engine_wx)
                ),
                RadioOptionItem(
                    value = WebUIEngine.KSU,
                    title = stringResource(R.string.settings_webui_engine_ksu)
                ),
                RadioOptionItem(
                    value = WebUIEngine.PREFER_MODULE,
                    title = stringResource(R.string.settings_webui_engine_prefer_module)
                )
            ),
            onConfirm = {
                viewModel.setWebUIEngine(it.value)
            }
        )
    }
}