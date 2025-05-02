package com.dergoogler.mmrl.ui.component.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import com.dergoogler.mmrl.ext.nullable

@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int? = null,
    style: TextWithIconStyle = TextWithIconDefaults.style,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = TextWithIcon(
    modifier = modifier,
    text = {
        Text(
            text = text,
            style = style.textStyle,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis
        )
    },
    icon = icon.nullable {
        {
            Icon(
                painter = painterResource(id = it),
                contentDescription = null,
                tint = style.iconTint,
            )
        }
    },
    style = style,
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = verticalAlignment
)

@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    text: (@Composable BoxScope.() -> Unit)? = null,
    icon: (@Composable BoxScope.() -> Unit)? = null,
    style: TextWithIconStyle = TextWithIconDefaults.style,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = TextWithIconLayout(
    modifier = modifier,
    text = text,
    icon = icon,
    style = style,
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = verticalAlignment
)

@Composable
internal fun TextWithIconLayout(
    modifier: Modifier = Modifier,
    text: (@Composable BoxScope.() -> Unit)? = null,
    icon: (@Composable BoxScope.() -> Unit)? = null,
    style: TextWithIconStyle = TextWithIconDefaults.style,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    if (text == null && icon == null) return

    val density = LocalDensity.current
    val iconSize = with(density) { style.textStyle.fontSize.toDp() * style.iconScaling }
    val spacerDp = style.spacing
    val spacerPx = with(density) { spacerDp.toDp().roundToPx() }

    Layout(
        modifier = modifier,
        content = {
            icon.nullable {
                Box(Modifier.size(iconSize)) { it() }
            }
            text.nullable {
                Box { it() }
            }
        }
    )  { measurables, constraints ->
        val iconPlaceable = if (icon != null) measurables[0].measure(constraints) else null
        val textPlaceable = if (text != null) {
            measurables.getOrNull(if (icon != null) 1 else 0)?.measure(constraints)
        } else null

        val iconWidth = iconPlaceable?.width ?: 0
        val textWidth = textPlaceable?.width ?: 0
        val spacerWidth = if (iconPlaceable != null && textPlaceable != null) spacerPx else 0

        val contentWidth = iconWidth + textWidth + spacerWidth
        val contentHeight = maxOf(iconPlaceable?.height ?: 0, textPlaceable?.height ?: 0)

        val layoutWidth = constraints.constrainWidth(contentWidth)
        val layoutHeight = constraints.constrainHeight(contentHeight)

        layout(layoutWidth, layoutHeight) {
            val startX = when (horizontalArrangement) {
                Arrangement.Center -> (layoutWidth - contentWidth) / 2
                Arrangement.End -> layoutWidth - contentWidth
                else -> 0
            }

            var x = startX

            val first = if (!style.rightIcon) iconPlaceable else textPlaceable
            val second = if (!style.rightIcon) textPlaceable else iconPlaceable

            first?.let {
                val y = verticalAlignment.align(it.height, layoutHeight)
                it.placeRelative(x, y)
                x += it.width + spacerWidth
            }

            second?.let {
                val y = verticalAlignment.align(it.height, layoutHeight)
                it.placeRelative(x, y)
            }
        }
    }
}

@Immutable
class TextWithIconStyle(
    val textStyle: TextStyle,
    val iconScaling: Float,
    val spacing: Float,
    val rightIcon: Boolean,
    val iconTint: Color,
    val overflow: TextOverflow,
    val maxLines: Int
) {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TextWithIconStyle) return false

        if (textStyle != other.textStyle) return false
        if (iconScaling != other.iconScaling) return false
        if (spacing != other.spacing) return false
        if (rightIcon != other.rightIcon) return false
        if (iconTint != other.iconTint) return false
        if (overflow != other.overflow) return false
        if (maxLines != other.maxLines) return false

        return true
    }

    fun copy(
        textStyle: TextStyle = this.textStyle,
        iconScaling: Float = this.iconScaling,
        spacing: Float = this.spacing,
        rightIcon: Boolean = this.rightIcon,
        iconTint: Color = this.iconTint,
        overflow: TextOverflow = this.overflow,
        maxLines: Int = this.maxLines
    ): TextWithIconStyle = TextWithIconStyle(
        textStyle,
        iconScaling,
        spacing,
        rightIcon,
        iconTint,
        overflow,
        maxLines
    )

    override fun hashCode(): Int {
        var result = textStyle.hashCode()
        result = 31 * result + iconScaling.hashCode()
        result = 31 * result + spacing.hashCode()
        result = 31 * result + rightIcon.hashCode()
        result = 31 * result + iconTint.hashCode()
        result = 31 * result + overflow.hashCode()
        result = 31 * result + maxLines.hashCode()
        return result
    }
}

object TextWithIconDefaults {
    val style
        @Composable get() = TextWithIconStyle(
            textStyle = LocalTextStyle.current,
            iconScaling = 1.4285715f,
            spacing = 11.428572f,
            rightIcon = false,
            iconTint = LocalContentColor.current,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip
        )
}