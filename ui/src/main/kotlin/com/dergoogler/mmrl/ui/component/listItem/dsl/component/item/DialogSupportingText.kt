package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.ui.component.listItem.dsl.DialogItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope

@Composable
fun ListItemScope.DialogSupportingText(
    content: @Composable BoxScope.() -> Unit,
) = Slot(
    slot = DialogItemSlot.SupportingText,
    content = content
)

@Composable
fun ListItemScope.DialogSupportingText(text: String) {
    this.DialogSupportingText {
        Text(text.toStyleMarkup())
    }
}

@Composable
fun ListItemScope.DialogSupportingText(
    @StringRes id: Int,
) = this.DialogSupportingText(stringResource(id))

@Composable
fun ListItemScope.DialogSupportingText(
    @StringRes id: Int,
    vararg formatArgs: Any,
) = this.DialogSupportingText(stringResource(id, *formatArgs))
