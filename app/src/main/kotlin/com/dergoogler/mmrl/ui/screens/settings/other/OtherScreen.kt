package com.dergoogler.mmrl.ui.screens.settings.other

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
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

        ListSwitchItem(
            title = stringResource(id = R.string.settings_doh),
            desc = stringResource(id = R.string.settings_doh_desc),
            checked = userPreferences.useDoh,
            onChange = viewModel::setUseDoh
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_provider_service),
            desc = stringResource(id = R.string.settings_provider_service_desc),
            checked = userPreferences.useProviderAsBackgroundService,
            enabled = viewModel.platform.isValid,
            onChange = {
                if (!it) {
                    ProviderService.stop(context)
                    scope.launch {
                        while (ProviderService.isActive.value) {
                            delay(100)
                        }
                        viewModel.setUseProviderAsBackgroundService(it)
                        snackbarHost.showSnackbar(context.getString(R.string.provider_service_stopped))
                    }

                    return@ListSwitchItem
                }

                viewModel.setUseProviderAsBackgroundService(it)
            }
        )
    }
}