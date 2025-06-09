package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScopeInstance
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Start
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.End
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import kotlin.math.max

/**
 * Represents a single item within a list.
 *
 * This composable function is designed to be used within the scope of a `ListScope` (e.g., inside a `List` composable).
 * It provides a structured way to define the layout of a list item, including optional start/end icons or actions,
 * a title, and a description.
 *
 * The layout is determined by the `ListItemSlot` enum, which defines the different sections of the item.
 * The `content` lambda is executed within a `ListItemScope`, allowing you to use composables like `Start`, `Title`,
 * `Description`, and `End` to place content in their respective slots.
 *
 * @param modifier Optional [Modifier] to be applied to the item.
 * @param enabled A boolean indicating whether the item is enabled. Disabled items will have reduced opacity.
 * @param content A lambda function that defines the content of the list item within a `ListItemScope`.
 *                This allows you to use slot-based composables like `Start`, `Title`, `Description`, and `End`.
 *
 * @see ListScope
 * @see ListItemScope
 * @see ListItemSlot
 * @see Start
 * @see Title
 * @see Description
 * @see End
 */
@Composable
fun ListScope.Item(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ListItemScope.() -> Unit,
) {
    val instance = remember {
        ListItemScopeInstance(
            contentPaddingValues = contentPaddingValues,
            iconSize = iconSize
        )
    }

    val layoutDirection = LocalLayoutDirection.current

    Layout(
        content = { instance.content() },
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .padding(contentPaddingValues)
    ) { measurables, constraints ->
        // 1. Find all measurable slots
        val startMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.Start }
        val titleMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.Title }
        val descriptionMeasurable =
            measurables.firstOrNull { it.layoutId == ListItemSlot.Description }
        val endMeasurable = measurables.firstOrNull { it.layoutId == ListItemSlot.End }

        // 2. Measure start content
        val startPlaceable = startMeasurable?.measure(constraints)
        val startWidth = startPlaceable?.width ?: 0
        val startPaddingPx = contentPaddingValues.calculateStartPadding(layoutDirection).roundToPx()
        val startSpacerWidth = if (startPlaceable != null) startPaddingPx else 0

        // 3. Measure end content
        val endPlaceable = endMeasurable?.measure(constraints)
        val endWidth = endPlaceable?.width ?: 0
        val endPaddingPx = contentPaddingValues.calculateEndPadding(layoutDirection).roundToPx()
        val endSpacerWidth = if (endPlaceable != null) endPaddingPx else 0

        // 4. Calculate available width for text
        val textMaxWidth =
            constraints.maxWidth - startWidth - startSpacerWidth - endWidth - endSpacerWidth
        val textConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = max(0, textMaxWidth)
        )

        // 5. Measure text content - TITLE FIRST then DESCRIPTION
        val titlePlaceable = titleMeasurable?.measure(textConstraints)
        val descriptionPlaceable = descriptionMeasurable?.measure(textConstraints)

        // 6. Calculate heights - title appears above description
        val titleHeight = titlePlaceable?.height ?: 0
        val descriptionHeight = descriptionPlaceable?.height ?: 0
        val textBlockHeight =
            titleHeight + (if (descriptionPlaceable != null) descriptionHeight + 4.dp.roundToPx() else 0)

        val totalHeight = maxOf(
            textBlockHeight,
            startPlaceable?.height ?: 0,
            endPlaceable?.height ?: 0
        )

        // 7. Place all elements
        layout(constraints.maxWidth, totalHeight) {
            // Place start content
            startPlaceable?.placeRelative(
                x = 0,
                y = (totalHeight - startPlaceable.height) / 2
            )

            // Place text content - TITLE FIRST
            val textStartX = startWidth + startSpacerWidth
            var textVerticalPos = (totalHeight - textBlockHeight) / 2

            titlePlaceable?.placeRelative(
                x = textStartX,
                y = textVerticalPos
            )

            // Then place DESCRIPTION BELOW title
            descriptionPlaceable?.placeRelative(
                x = textStartX,
                y = textVerticalPos + titleHeight
            )

            // Place end content
            endPlaceable?.placeRelative(
                x = constraints.maxWidth - endWidth,
                y = (totalHeight - endPlaceable.height) / 2
            )
        }
    }
}