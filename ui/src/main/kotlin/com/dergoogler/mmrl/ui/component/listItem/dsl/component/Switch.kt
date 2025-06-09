package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.End

/**
 * A List item with a Switch component at the end.
 *
 * This component displays a list item with a switch on the right side.
 * The entire item is toggleable. The `content` lambda defines the main
 * content of the list item, which is placed to the left of the switch.
 *
 * **Note:** Do not define a `ListItemScope.End` within the `content` lambda,
 * as this component automatically adds a Switch component in that position.
 *
 * @param checked Whether the switch is currently checked.
 * @param enabled Whether the list item and switch are enabled.
 * @param onChange Callback invoked when the checked state of the switch changes.
 * @param interactionSource The [MutableInteractionSource] representing the stream of
 *   [Interaction]s for this component. You can create and pass in your own `remember`ed
 *   instance to observe [Interaction]s and customize the appearance / behavior of this
 *   component in different [Interaction]s.
 * @param content The composable content to display within the list item, to the left of the switch.
 *   This will be placed in the `ListItemScope.Start` or `ListItemScope.Center` position implicitly.
 */
@Composable
fun ListScope.Switch(
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ListItemScope.() -> Unit,
) {
    Item(
        enabled = enabled,
        modifier = Modifier.toggleable(
            value = checked,
            enabled = enabled,
            onValueChange = onChange,
            role = Role.Switch,
            interactionSource = interactionSource,
            indication = ripple()
        )
    ) {
        content()
        End {
            Switch(
                checked = checked,
                onCheckedChange = null
            )
        }
    }
}