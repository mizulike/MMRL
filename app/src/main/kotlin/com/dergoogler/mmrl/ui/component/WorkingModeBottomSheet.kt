package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.SettingsViewModel
import dev.dergoogler.mmrl.compat.ext.nullable
import kotlin.system.exitProcess

@Composable
fun WorkingModeBottomSheet(
    onClose: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) = BottomSheet(
    onDismissRequest = onClose,
    enabledNavigationSpacer = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        WorkingModeItems(
            isSetup = false,
            setMode = viewModel::setWorkingMode,
        )
    }
}

@Composable
fun WorkingModeItems(
    isSetup: Boolean,
    setMode: (WorkingMode) -> Unit,
) {
    val userPreferences = LocalUserPreferences.current

    WorkingModeItem(
        icon = R.drawable.magisk_logo,
        title = stringResource(R.string.working_mode_magisk_title),
        desc = stringResource(R.string.working_mode_magisk_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_MAGISK),
        setMode = setMode,
        mode = WorkingMode.MODE_MAGISK
    )

    WorkingModeItem(
        icon = R.drawable.kernelsu_logo,
        title = stringResource(R.string.working_mode_kernelsu_title),
        desc = stringResource(R.string.working_mode_kernelsu_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_KERNEL_SU),
        setMode = setMode,
        mode = WorkingMode.MODE_KERNEL_SU
    )

    WorkingModeItem(
        icon = R.drawable.kernelsu_next_logo,
        title = stringResource(R.string.working_mode_kernelsu_next_title),
        desc = stringResource(R.string.working_mode_kernelsu_next_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_KERNEL_SU_NEXT),
        setMode = setMode,
        mode = WorkingMode.MODE_KERNEL_SU_NEXT
    )

    WorkingModeItem(
        icon = R.drawable.brand_android,
        title = stringResource(R.string.working_mode_apatch_title),
        desc = stringResource(R.string.working_mode_apatch_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_APATCH),
        setMode = setMode,
        mode = WorkingMode.MODE_APATCH
    )

    WorkingModeItem(
        icon = R.drawable.shield_lock,
        title = stringResource(id = R.string.setup_non_root_title),
        desc = stringResource(id = R.string.setup_non_root_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_NON_ROOT),
        setMode = setMode,
        mode = WorkingMode.MODE_NON_ROOT
    )

    WorkingModeItem(
        icon = R.drawable.shizuku,
        title = stringResource(id = R.string.working_mode_shizuku_title),
        desc = stringResource(id = R.string.working_mode_shizuku_desc),
        selected = !isSetup && (userPreferences.workingMode == WorkingMode.MODE_SHIZUKU),
        setMode = setMode,
        mode = WorkingMode.MODE_SHIZUKU
    )
}

@Composable
fun WorkingModeItem(
    title: String,
    desc: String,
    @DrawableRes icon: Int? = null,
    selected: Boolean = false,
    mode: WorkingMode,
    setMode: (WorkingMode) -> Unit,
) {
    var restartDialog by remember { mutableStateOf(false) }

    if (restartDialog) ConfirmDialog(
        title = R.string.working_mode_change_dialog_title,
        description = R.string.working_mode_change_dialog_desc,
        closeText = R.string.keep,
        onClose = {
            restartDialog = false
        },
        confirmText = R.string.apply,
        onConfirm = {
            restartDialog = false
            setMode(mode)
            exitProcess(0)
        }
    )


    ListButtonItem(
        icon = icon,
        title = title,
        desc = desc,
        onClick = {
            restartDialog = true
        },
        base = {
            labels = selected nullable listOf {
                LabelItem(
                    text = stringResource(id = R.string.selected)
                )
            }
        }
    )
}