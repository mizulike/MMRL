package com.dergoogler.mmrl.ui.screens.settings.other

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Switch
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.settings.appearance.items.DownloadPathItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtherScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = LocalSnackbarHost.current
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_other
    ) {
        DownloadPathItem(
            downloadPath = userPreferences.downloadPath,
            onChange = viewModel::setDownloadPath
        )

        Switch(
            checked = userPreferences.useDoh,
            onChange = viewModel::setUseDoh
        ) {
            Title(R.string.settings_doh)
            Description(R.string.settings_doh_desc)
        }

        Switch(
            checked = ProviderService.isActive,
            onChange = {
                scope.launch {
                    if (it) {
                        ProviderService.start(context, userPreferences.workingMode)
                        snackbarHost.showSnackbar(context.getString(R.string.provider_service_started))
                    } else {
                        ProviderService.stop(context)
                        while (ProviderService.isActive) {
                            delay(100)
                        }
                        snackbarHost.showSnackbar(context.getString(R.string.provider_service_stopped))
                    }
                }
            }

        ) {
            Title(R.string.settings_provider_service)
            Description(R.string.settings_provider_service_desc)
        }
    }
}