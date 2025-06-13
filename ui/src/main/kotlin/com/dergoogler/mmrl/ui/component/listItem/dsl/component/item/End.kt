package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

/**
 * Composable function that defines the end slot of a list item.
 * This slot is typically used for trailing icons, actions, or other content
 * that should be aligned to the end of the list item.
 *
 * @param content The composable content to be displayed in the end slot.
 *                The content is placed within a `Box` that is aligned to the end.
 */
@Composable
fun ListItemScope.End(content: @Composable BoxScope.() -> Unit) =
    Slot(
        slot = ListItemSlot.End,
        content = content
    )
