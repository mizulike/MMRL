package com.dergoogler.mmrl.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.applyAlpha
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.token.applyTonalElevation

enum class CardSlot {
    Relative,
    Absolute
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Card(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    shape: Shape = RoundedCornerShape(20.dp),
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    absoluteAlignment: Alignment = Alignment.TopStart,
    absolute: @Composable (BoxScope.() -> Unit)? = null,
    relative: @Composable BoxScope.() -> Unit,
) {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation
    val clickableModifier = if (onClick != null || onLongClick != null) {
        Modifier
            .combinedClickable(
                enabled = enabled,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .hoverable(interactionSource)
    } else {
        Modifier
    }

    val hoveredBorder = if (isHovered) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else border

    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        Layout(
            modifier = Modifier
                .card(
                    shape = shape,
                    backgroundColor = MaterialTheme.colorScheme.applyTonalElevation(
                        color,
                        absoluteElevation
                    ),
                    border = hoveredBorder,
                    shadowElevation = with(LocalDensity.current) { shadowElevation.toPx() }
                )
                .applyAlpha(enabled)
                .semantics(mergeDescendants = false) {
                    @Suppress("DEPRECATION")
                    isContainer = true
                }
                .then(clickableModifier)
                .pointerInput(Unit) {}
                .then(modifier),
            content = {
                Box(modifier = Modifier.layoutId(CardSlot.Relative)) {
                    relative()
                }
                absolute.nullable {
                    Box(modifier = Modifier.layoutId(CardSlot.Absolute)) {
                        it()
                    }
                }
            }
        ) { measurables, constraints ->
            val relativeMeasurable = measurables.firstOrNull { it.layoutId == CardSlot.Relative }
            val absoluteMeasurable = measurables.firstOrNull { it.layoutId == CardSlot.Absolute }

            val relativePlaceable = relativeMeasurable?.measure(constraints)
            val absolutePlaceable = absoluteMeasurable?.measure(constraints)

            val width = maxOf(relativePlaceable?.width ?: 0, absolutePlaceable?.width ?: 0)
            val height = maxOf(relativePlaceable?.height ?: 0, absolutePlaceable?.height ?: 0)

            layout(width, height) {
                relativePlaceable?.placeRelative(0, 0)

                absolutePlaceable.nullable { placeable ->
                    val childSize = IntSize(placeable.width, placeable.height)

                    val parentSize = IntSize(width, height)

                    val position = absoluteAlignment.align(
                        size = childSize,
                        space = parentSize,
                        layoutDirection = layoutDirection
                    )

                    placeable.place(position.x, position.y)
                }
            }
        }
    }
}

fun Modifier.card(
    shape: Shape,
    backgroundColor: Color,
    border: BorderStroke?,
    shadowElevation: Float,
) = this
    .then(
        if (shadowElevation > 0f) {
            Modifier.graphicsLayer(
                shadowElevation = shadowElevation,
                shape = shape,
                clip = false
            )
        } else {
            Modifier
        }
    )
    .then(if (border != null) Modifier.border(border, shape) else Modifier)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)
