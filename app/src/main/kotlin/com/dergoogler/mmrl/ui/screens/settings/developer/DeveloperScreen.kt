package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun ListScope.DeveloperSwitch(
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
    checked: Boolean,
    content: @Composable ListItemScope.() -> Unit,
) {
    val userPrefs = LocalUserPreferences.current

    SwitchItem(
        checked = userPrefs.developerMode && checked,
        onChange = onChange,
        enabled = userPrefs.developerMode && enabled,
        content = content
    )
}

@Composable
fun DeveloperScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_developer
    ) {
        SwitchItem(
            checked = userPreferences.developerMode,
            onChange = viewModel::setDeveloperMode,
        ) {
            Title(R.string.settings_developer_mode)
            Description(R.string.settings_developer_mode_desc)
        }

        DeveloperSwitch(
            checked = userPreferences.devAlwaysShowUpdateAlert,
            onChange = viewModel::setDevAlwaysShowUpdateAlert,
        ) {
            Title(R.string.settings_always_show_update_alert)
        }
    }
}
