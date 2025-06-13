package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

@Composable
fun ListItemScope.Slot(
    slot: ListItemSlot,
    disallow: List<ListItemSlot> = emptyList(),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = Modifier.layoutSlot(slot, disallow)) {
        content()
    }
}