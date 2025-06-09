package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope

/**
 * A composable function that creates a [Box] with a modifier that aligns its content to the start.
 * This is typically used to place content at the beginning of a [ListItemScope], such as an icon or avatar.
 *
 * @param content The composable content to be displayed within the start-aligned box.
 *                The content is scoped to [BoxScope] to allow for further customization within the box.
 */
@Composable
fun ListItemScope.Start(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.start()) {
        content()
    }
}

/**
 * Composable function that defines the end slot of a list item.
 * This slot is typically used for trailing icons, actions, or other content
 * that should be aligned to the end of the list item.
 *
 * @param content The composable content to be displayed in the end slot.
 *                The content is placed within a `Box` that is aligned to the end.
 */
@Composable
fun ListItemScope.End(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.end()) {
        content()
    }
}
