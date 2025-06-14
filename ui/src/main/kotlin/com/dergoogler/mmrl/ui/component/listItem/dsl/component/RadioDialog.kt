package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Start
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title

data class RadioDialogItem<T>(
    val value: T,
    val title: String? = null,
    val desc: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun <T> ListScope.RadioDialog(
    title: String = stringResource(R.string.select_a_option),
    selection: T,
    enabled: Boolean = true,
    options: List<RadioDialogItem<T>>,
    onConfirm: (RadioDialogItem<T>) -> Unit,
    content: @Composable (ListItemScope.() -> Unit),
) {
    var open by remember { mutableStateOf(false) }

    if (open) {
        AlertRadioDialog<T>(
            title = title,
            selection = selection,
            options = options,
            onClose = {
                open = false
            },
            onConfirm = onConfirm
        )
    }

    Button(
        enabled = enabled,
        onClick = {
            open = true
        },
        content = content
    )
}

@Composable
private fun <T> ListScope.AlertRadioDialog(
    title: String,
    selection: T,
    options: List<RadioDialogItem<T>>,
    onDismiss: (() -> Unit)? = null,
    onClose: () -> Unit,
    onConfirm: (RadioDialogItem<T>) -> Unit,
) {
    var selectedOption by remember { mutableStateOf(selection) }

    val onDone: () -> Unit = {
        onConfirm(RadioDialogItem(selectedOption))
        onClose()
    }

    AlertDialog(
        onDismissRequest = {
            if (onDismiss != null) {
                onDismiss()
                return@AlertDialog
            }

            onClose()
        },
        title = { Text(title) },
        text = {
            LazyColumn {
                items(
                    items = options,
                ) { option ->
                    val checked = option.value == selectedOption
                    val interactionSource = remember { MutableInteractionSource() }

                    if (option.title == null) return@items

                    Row(
                        modifier = Modifier
                            .toggleable(
                                enabled = option.enabled,
                                value = checked,
                                onValueChange = {
                                    selectedOption = option.value
                                },
                                role = Role.RadioButton,
                                interactionSource = interactionSource,
                                indication = ripple()
                            )
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        this@AlertRadioDialog.Item(
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.5.dp)
                        ) {
                            Title(option.title)

                            option.desc.nullable {
                                Description(it)
                            }

                            Start {
                                RadioButton(
                                    enabled = option.enabled,
                                    selected = checked,
                                    onClick = null
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDone) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )

}