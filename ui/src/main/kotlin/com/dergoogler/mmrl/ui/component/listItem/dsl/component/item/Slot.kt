package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

@Composable
fun ListItemScope.Slot(
    slot: Any,
    modifier: Modifier = Modifier,
    disallow: List<Any> = emptyList(),
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = Modifier
        .layoutSlot(slot, disallow)
        .then(modifier),
    content = content
)

@Composable
fun ListItemScope.FromSlot(
    slot: Any,
    content: @Composable ListItemScope.() -> Unit,
) = SubcomposeLayout { constraints ->
    val measurables = subcompose(slot) {
        content()
    }

    val measurable = measurables.firstOrNull { it.layoutId == slot }

    val placeable = measurable?.measure(constraints)

    layout(placeable?.width ?: 0, placeable?.height ?: 0) {
        placeable?.placeRelative(0, 0)
    }
}
