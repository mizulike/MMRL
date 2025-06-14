package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.PaddingValues
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
import kotlin.math.max

@Composable
fun ListScope.Item(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = contentPaddingValues,
    content: @Composable ListItemScope.() -> Unit,
) {
    val instance = remember {
        ListItemScopeInstance(
            contentPaddingValues = contentPadding,
            iconSize = iconSize
        )
    }

    val layoutDirection = LocalLayoutDirection.current

    Layout(
        content = { instance.content() },
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .padding(contentPadding)
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
        val startPaddingPx = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
        val startSpacerWidth = if (startPlaceable != null) startPaddingPx else 0

        // 3. Measure end content
        val endPlaceable = endMeasurable?.measure(constraints)
        val endWidth = endPlaceable?.width ?: 0
        val endPaddingPx = contentPadding.calculateEndPadding(layoutDirection).roundToPx()
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