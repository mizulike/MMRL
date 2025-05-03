package com.dergoogler.mmrl.ui.component.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.dergoogler.mmrl.ext.nullable

@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int? = null,
    style: TextWithIconStyle = TextWithIconDefaults.style,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    val density = LocalDensity.current
    val iconSize = with(density) { style.textStyle.fontSize.toDp() * style.iconScaling }

    val decoratedIconContent: @Composable (() -> Unit)? =
        icon.nullable {
            {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = style.iconTint,
                )
            }
        }

    TextRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        leadingContent = !style.rightIcon nullable decoratedIconContent,
        trailingContent = style.rightIcon nullable decoratedIconContent,
    ) {
        Text(
            text = text,
            style = style.textStyle,
            maxLines = style.maxLines,
            overflow = style.overflow
        )
    }
}

@Immutable
class TextWithIconStyle(
    val textStyle: TextStyle,
    val iconScaling: Float,
    @Deprecated("Deprecated")
    val spacing: Float,
    val rightIcon: Boolean,
    val iconTint: Color,
    val overflow: TextOverflow,
    val maxLines: Int,
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
        maxLines: Int = this.maxLines,
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