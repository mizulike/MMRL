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
