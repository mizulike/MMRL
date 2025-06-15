package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.dialog.TextFieldDialog
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.FromSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.ProvideTitleTypography
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens

@Composable
fun ListScope.TextEditDialogItem(
    enabled: Boolean = true,
    description: (@Composable (String) -> Unit)? = null,
    value: String,
    onConfirm: (String) -> Unit,
    content: @Composable (ListItemScope.() -> Unit),
) {
    var open by remember { mutableStateOf(false) }
    ButtonItem(
        enabled = enabled,
        onClick = {
            open = true
        },
        content = {
            content()

            if (open) {
                this@TextEditDialogItem.AlertTextEditDialog(
                    title = {
                        ProvideTitleTypography(
                            token = TypographyKeyTokens.HeadlineSmall
                        ) {
                            this@ButtonItem.FromSlot(ListItemSlot.Title, content)
                        }
                    },
                    description = description,
                    value = value,
                    onClose = {
                        open = false
                    },
                    onConfirm = onConfirm
                )
            }
        }
    )
}


@Composable
private fun ListScope.AlertTextEditDialog(
    title: @Composable () -> Unit,
    description: (@Composable (String) -> Unit)? = null,
    value: String,
    onValid: ((String) -> Boolean)? = null,
    onClose: () -> Unit,
    supportingText: (@Composable (Boolean) -> Unit)? = null,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(value) }
    var isError by remember { mutableStateOf(false) }

    val onDone: () -> Unit = {
        onConfirm(text)
        onClose()
    }

    onValid.nullable { c ->
        LaunchedEffect(c) {
            isError = c(value)
        }
    }

    TextFieldDialog(
        shape = RoundedCornerShape(20.dp),
        onDismissRequest = onClose,
        title = title,
        confirmButton = {
            TextButton(
                onClick = onDone,
                enabled = !isError && text.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    ) { focusRequester ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            description.nullable {
                it(text)
            }

            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge,
                value = text,
                onValueChange = {
                    onValid.nullable { c ->
                        isError = c(it)
                    }
                    text = it
                },
                singleLine = false,
                supportingText = {
                    supportingText.nullable {
                        it(isError)
                    }
                },
                isError = isError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    if (text.isNotBlank()) onDone()
                },
                shape = RoundedCornerShape(15.dp)
            )
        }
    }
}