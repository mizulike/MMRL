package com.dergoogler.mmrl.ui.screens.settings.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.KernelSuLabel
import com.dergoogler.mmrl.ui.component.KernelSuNextLabel
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.ListHeader
import com.dergoogler.mmrl.ui.component.listItem.ListSwitchItem
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import dev.dergoogler.mmrl.compat.impl.ksu.KsuNative
import timber.log.Timber

@Composable
fun SecurityScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    val navController = LocalNavController.current

    SettingsScaffold(
        title = R.string.settings_security,
    ) {
        ListSwitchItem(
            title = stringResource(id = R.string.settings_reboot_protection),
            desc = stringResource(id = R.string.settings_reboot_protection_desc),
            checked = userPreferences.confirmReboot,
            onChange = viewModel::setConfirmReboot
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_blacklist_alerts),
            desc = stringResource(id = R.string.settings_blacklist_alerts_desc),
            checked = userPreferences.blacklistAlerts,
            onChange = viewModel::setBlacklistAlerts
        )

        ListSwitchItem(
            title = stringResource(id = R.string.settings_hide_fingerprint),
            desc = stringResource(id = R.string.settings_hide_fingerprint_desc),
            checked = userPreferences.hideFingerprintInHome,
            onChange = viewModel::setHideFingerprintInHome
        )

        if (viewModel.platform.isKernelSuOrNext) {
            var isSuDisabled by remember {
                mutableStateOf(!viewModel.isSuEnabled)
            }

            val isSuCompatSupported = if (viewModel.platform.isKernelSU) {
                viewModel.versionCode >= KsuNative.MINIMAL_SUPPORTED_SU_COMPAT
            } else if (viewModel.platform.isKernelSuNext) {
                viewModel.versionCode >= KsuNative.MINIMAL_SUPPORTED_SU_COMPAT_NEXT
            } else {
                false
            }

            ListHeader(title = stringResource(id = R.string.working_mode_kernelsu_title))

            ListSwitchItem(
                enabled = isSuCompatSupported,
                title = "Disable SU Compatibility",
                desc = "Temporarily disable the ability of any app to gain root privileges via the su command (existing root processes won't be affected)",
                checked = isSuDisabled,
                onChange = { checked ->
                    Timber.d("Checked: $checked")
                    val shouldEnable = !checked
                    if (viewModel.setSuEnabled(shouldEnable)) {
                        Timber.d("Set su enabled: $shouldEnable")
                        isSuDisabled = !shouldEnable
                    }
                },
                base = {
                    labels = listOf { KernelSuLabel(); KernelSuNextLabel() }
                }
            )
        }
    }
}