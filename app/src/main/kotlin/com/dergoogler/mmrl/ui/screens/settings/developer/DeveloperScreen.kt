package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Switch
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun DeveloperScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_developer
    ) {
        Switch(
            checked = userPreferences.developerMode,
            onChange = viewModel::setDeveloperMode,
        ) {
            Title(R.string.settings_developer_mode)
            Description(R.string.settings_developer_mode_desc)
        }
    }
}
