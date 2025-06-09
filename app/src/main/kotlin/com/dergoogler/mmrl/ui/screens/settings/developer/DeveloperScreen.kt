package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun DeveloperScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_developer
    ) {
        ListSwitchItem(
            title = stringResource(id = R.string.settings_developer_mode),
            desc = stringResource(id = R.string.settings_developer_mode_desc),
            checked = userPreferences.developerMode,
            onChange = viewModel::setDeveloperMode,
        )
    }
}
