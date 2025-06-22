package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.ui.component.listItem.dsl.DialogItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope

@Composable
fun ListItemScope.DialogDescription(
    content: @Composable ListItemSlotScope.() -> Unit,
) = Slot(
    slot = DialogItemSlot.Description,
    content = content
)

@Composable
fun ListItemScope.DialogDescription(text: String) {
    this.DialogDescription {
        Text(text.toStyleMarkup())
    }
}

@Composable
fun ListItemScope.DialogDescription(
    @StringRes id: Int,
) = this.DialogDescription(stringResource(id))

@Composable
fun ListItemScope.DialogDescription(
    @StringRes id: Int,
    vararg formatArgs: Any,
) = this.DialogDescription(stringResource(id, *formatArgs))
