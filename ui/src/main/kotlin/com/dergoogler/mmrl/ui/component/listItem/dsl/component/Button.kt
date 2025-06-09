package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope

@Composable
fun ListScope.Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    content: @Composable ListItemScope.() -> Unit,
) {
    this.Item(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                role = Role.Button,
                indication = ripple(),
                onClick = onClick
            )
            .then(modifier),
        enabled = enabled,
        content = content
    )
}