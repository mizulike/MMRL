package com.dergoogler.mmrl.ui.screens.settings.updates

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.service.ModuleService
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.service.RepositoryService
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.component.listItem.ListRadioCheckItem
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.component.listItem.RadioOptionItem
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun radioOptionItem(interval: Long): RadioOptionItem<Long> {
    return RadioOptionItem(
        title = pluralStringResource(id = R.plurals.hours, count = interval.toInt(), interval),
        value = interval
    )
}

@Composable
fun UpdatesScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current
    val snackbarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val optionsOfHours = listOf(
        radioOptionItem(1),
        radioOptionItem(2),
        radioOptionItem(3),
        radioOptionItem(4),
        radioOptionItem(5),
        radioOptionItem(6),
        radioOptionItem(10),
        radioOptionItem(12),
        radioOptionItem(16),
        radioOptionItem(24),
        radioOptionItem(48),
        radioOptionItem(72),
    )

    SettingsScaffold(
        title = R.string.settings_updates,
    ) {
        ListButtonItem(
            title = stringResource(id = R.string.settings_open_notification_settings),
            onClick = {
                val intent =
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent, null)
                } else {
                    Toast.makeText(
                        context,
                        "Cannot open notification settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        ListHeader(
            title = stringResource(id = R.string.settings_app)
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_check_app_updates),
            desc = stringResource(id = R.string.settings_check_app_updates_desc),
            checked = userPreferences.checkAppUpdates,
            onChange = viewModel::setCheckAppUpdates
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_include_preleases),
            enabled = userPreferences.checkAppUpdates,
            checked = userPreferences.checkAppUpdatesPreReleases,
            onChange = viewModel::setCheckAppUpdatesPreReleases
        )

        ListHeader(
            title = stringResource(id = R.string.page_repository)
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_auto_update_repos),
            desc = stringResource(id = R.string.settings_auto_update_repos_desc),
            checked = RepositoryService.isActive,
            onChange = {
                scope.launch {
                    if (it) {
                        RepositoryService.start(context, userPreferences.autoUpdateReposInterval)
                        snackbarHost.showSnackbar(context.getString(R.string.repository_service_started))
                    } else {
                        RepositoryService.stop(context)
                        while (RepositoryService.isActive) {
                            delay(100)
                        }
                        snackbarHost.showSnackbar(context.getString(R.string.repository_service_stopped))
                    }
                }
            }
        )

        ListRadioCheckItem(
            title = stringResource(R.string.settings_repo_update_interval),
            desc = stringResource(
                R.string.settings_repo_update_interval_desc,
                userPreferences.autoUpdateReposInterval
            ),
            enabled = RepositoryService.isActive,
            value = userPreferences.autoUpdateReposInterval,
            options = optionsOfHours,
            onConfirm = {
                viewModel.setAutoUpdateReposInterval(it.value)
            })

        ListHeader(
            title = stringResource(id = R.string.page_modules)
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_check_modules_update),
            desc = stringResource(id = R.string.settings_check_modules_update_desc),
            checked = ModuleService.isActive,
            enabled = viewModel.isProviderAlive && ProviderService.isActive,
            onChange = {
                scope.launch {
                    if (it) {
                        ModuleService.start(context, userPreferences.autoUpdateReposInterval)
                        snackbarHost.showSnackbar(context.getString(R.string.module_service_started))
                    } else {
                        ModuleService.stop(context)
                        while (ModuleService.isActive) {
                            delay(100)
                        }
                        snackbarHost.showSnackbar(context.getString(R.string.module_service_stopped))
                    }
                }
            }
        )

        ListRadioCheckItem(
            title = stringResource(R.string.settings_check_modules_update_interval),
            desc = stringResource(
                R.string.settings_check_modules_update_interval_desc,
                userPreferences.checkModuleUpdatesInterval
            ),
            enabled = viewModel.isProviderAlive && ModuleService.isActive && ProviderService.isActive,
            value = userPreferences.checkModuleUpdatesInterval,
            options = optionsOfHours,
            onConfirm = {
                viewModel.setCheckModuleUpdatesInterval(it.value)
            }
        )
    }
}