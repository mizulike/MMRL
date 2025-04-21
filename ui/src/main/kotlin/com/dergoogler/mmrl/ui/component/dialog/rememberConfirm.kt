package com.dergoogler.mmrl.ui.component.dialog

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.R

data class ConfirmData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit,
    val onClose: () -> Unit,
    val confirmText: String? = null,
    val closeText: String? = null
)

@Composable
fun rememberConfirm(
    context: Context = LocalContext.current,
): (ConfirmData) -> Unit {
    val theme = MaterialTheme.colorScheme

    val confirm: (ConfirmData) -> Unit = remember {
        { confirm ->
            (context as? Activity)?.addContentView(
                ComposeView(context).apply {
                    setContent {
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            MaterialTheme(colorScheme = theme) {
                                ConfirmDialog(
                                    onDismissRequest = {
                                        showDialog = false
                                        confirm.onClose()
                                    },
                                    closeText = confirm.closeText ?: stringResource(id = R.string.cancel),
                                    confirmText = confirm.confirmText ?: stringResource(id = R.string.confirm),
                                    title = confirm.title,
                                    description = confirm.description,
                                    onClose = {
                                        showDialog = false
                                        confirm.onClose()
                                    },
                                    onConfirm = {
                                        showDialog = false
                                        confirm.onConfirm()
                                    }
                                )
                            }
                        }
                    }
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    return confirm
}
