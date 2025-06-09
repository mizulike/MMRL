package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.DefaultIconSize

/**
 * A composable function that adds an icon to a list item.
 *
 * @param painter The painter to use for the icon.
 * @param size The size of the icon. Defaults to [DefaultIconSize].
 * @param slot The slot to place the icon in. Can be either [ListItemSlot.Start] or [ListItemSlot.End].
 * Defaults to [ListItemSlot.Start].
 * @throws IllegalArgumentException if the slot is not [ListItemSlot.Start] or [ListItemSlot.End].
 */
@Composable
fun ListItemScope.Icon(
    painter: Painter,
    size: Dp = iconSize,
    slot: ListItemSlot = ListItemSlot.Start,
) {
    val baseIcon: @Composable BoxScope.() -> Unit = {
        Icon(
            modifier = Modifier.size(size),
            painter = painter,
            contentDescription = null,
            tint = LocalContentColor.current
        )
    }

    when (slot) {
        ListItemSlot.Start -> this.Start(baseIcon)
        ListItemSlot.End -> this.End(baseIcon)
        else -> throw IllegalArgumentException("Icon can only be used in Start or End slot")
    }
}
