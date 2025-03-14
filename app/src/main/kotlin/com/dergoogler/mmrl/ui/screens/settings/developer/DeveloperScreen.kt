package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.ListEditTextSwitchItem
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import dev.dergoogler.mmrl.compat.ext.isLocalWifiUrl
import dev.dergoogler.mmrl.compat.ext.takeTrue

@Composable
fun DeveloperScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    var webuiRemoteUrlInfo by remember { mutableStateOf(false) }
    if (webuiRemoteUrlInfo) AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.settings_webui_remote_url))
        },
        text = {
            Text(text = stringResource(id = R.string.settings_webui_remote_url_alert_desc))
        },
        onDismissRequest = {
            webuiRemoteUrlInfo = false
        },
        confirmButton = {
            TextButton(
                onClick = {
                    webuiRemoteUrlInfo = false
                }
            ) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        },
    )

    SettingsScaffold(
        title = R.string.settings_developer
    ) {
        ListSwitchItem(
            title = stringResource(id = R.string.settings_developer_mode),
            desc = stringResource(id = R.string.settings_developer_mode_desc),
            checked = userPreferences.developerMode,
            onChange = viewModel::setDeveloperMode,
        )

        ListHeader(title = stringResource(id = R.string.view_module_features_webui))

        ListEditTextSwitchItem(
            enabled = userPreferences.developerMode,
            title = stringResource(id = R.string.settings_webui_remote_url),
            desc = stringResource(id = R.string.settings_webui_remote_url_desc),
            value = userPreferences.webUiDevUrl,
            checked = userPreferences.useWebUiDevUrl,
            onChange = viewModel::setUseWebUiDevUrl,
            onConfirm = {
                viewModel.setWebUiDevUrl(it)
            },
            onValid = { !it.isLocalWifiUrl() },
            base = {
                learnMore = {
                    webuiRemoteUrlInfo = true
                }
            },
            dialog = {
                supportingText = { isError ->
                    isError.takeTrue {
                        Text(
                            text = stringResource(R.string.invalid_ip),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
        )
    }
}
