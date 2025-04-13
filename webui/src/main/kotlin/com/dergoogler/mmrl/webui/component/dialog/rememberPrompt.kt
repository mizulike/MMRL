package com.dergoogler.mmrl.webui.component.dialog

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.webui.R

internal data class PromptData(
    val title: String,
    val value: String,
    val onValid: ((String) -> Boolean)? = null,
    val onConfirm: (String) -> Unit,
    val onClose: () -> Unit,
    val dialog: DialogParameters.() -> Unit = {},
)

@Composable
internal fun rememberPrompt(
    context: Context = LocalContext.current,
): (PromptData) -> Unit {
    val theme = MaterialTheme.colorScheme

    val confirm: (PromptData) -> Unit = remember {
        { prompt ->
            (context as? Activity)?.addContentView(
                ComposeView(context).apply {
                    setContent {
                        val dialogParameters = remember { DialogParameters() }.apply(prompt.dialog)
                        var text by remember { mutableStateOf(prompt.value) }
                        var isError by remember { mutableStateOf(false) }
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            MaterialTheme(colorScheme = theme) {

                                val onDone = {
                                    showDialog = false
                                    prompt.onConfirm(text)
                                }

                                val onClose = {
                                    showDialog = false
                                    prompt.onClose()
                                }

                                prompt.onValid?.let { c ->
                                    LaunchedEffect(c) {
                                        isError = c(prompt.value)
                                    }
                                }

                                TextFieldDialog(shape = RoundedCornerShape(20.dp),
                                    onDismissRequest = onClose,
                                    title = { Text(text = prompt.title) },
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
                                    }) { focusRequester ->
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        dialogParameters.desc?.let {
                                            it(text)
                                        }

                                        OutlinedTextField(
                                            modifier = Modifier.focusRequester(focusRequester),
                                            textStyle = MaterialTheme.typography.bodyLarge,
                                            value = text,
                                            onValueChange = {
                                                prompt.onValid?.let { c ->
                                                    isError = c(it)
                                                }
                                                text = it
                                            },
                                            singleLine = false,
                                            supportingText = {
                                                dialogParameters.supportingText?.let {
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
                        }
                    }
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    return confirm
}
