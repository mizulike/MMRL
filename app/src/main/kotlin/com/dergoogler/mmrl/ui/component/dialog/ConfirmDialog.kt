package com.dergoogler.mmrl.ui.component.dialog

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R

@Composable
fun ConfirmDialog(
    title: @Composable (() -> Unit)?,
    description: @Composable (() -> Unit)?,
    confirmText: @Composable () -> Unit = {
        Text(text = stringResource(R.string.install_screen_reboot_confirm))
    },
    closeText: @Composable () -> Unit = {
        Text(text = stringResource(R.string.dialog_cancel))
    },
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        title = title,
        text = description,
        onDismissRequest = {
            onClose()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                confirmText.invoke()
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose()
                }
            ) {
                closeText.invoke()
            }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    description: String,
    confirmText: String = stringResource(id = R.string.install_screen_reboot_confirm),
    closeText: String = stringResource(id = R.string.dialog_cancel),
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) = ConfirmDialog(
    title = {
        Text(text = title)
    },
    description = {
        Text(text = description)
    },
    confirmText = {
        Text(text = confirmText)
    },
    closeText = {
        Text(text = closeText)
    },
    onClose = onClose,
    onConfirm = onConfirm
)

@Composable
fun ConfirmDialog(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes confirmText: Int = R.string.install_screen_reboot_confirm,
    @StringRes closeText: Int = R.string.dialog_cancel,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) = ConfirmDialog(
    title = {
        Text(text = stringResource(title))
    },
    description = {
        Text(text = stringResource(description))
    },
    confirmText = {
        Text(text = stringResource(confirmText))
    },
    closeText = {
        Text(text = stringResource(closeText))
    },
    onClose = onClose,
    onConfirm = onConfirm
)