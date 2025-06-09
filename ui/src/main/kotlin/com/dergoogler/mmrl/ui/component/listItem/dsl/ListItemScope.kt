package com.dergoogler.mmrl.ui.component.listItem.dsl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.ext.toStyleMarkup

@Composable
fun ListItemScope.Start(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.start()) {
        content()
    }
}

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

@Composable
fun ListItemScope.End(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.end()) {
        content()
    }
}


@Composable
fun ListItemScope.Title(
    content: @Composable BoxScope.() -> Unit,
) {
    val style = MaterialTheme.typography.bodyLarge.copy(
        color = LocalContentColor.current
    )

    Box(modifier = Modifier.title()) {
        ProvideTextStyle(style) {
            content()
        }
    }
}

@Composable
fun ListItemScope.Title(
    text: String,
) {
    this.Title {
        Text(text)
    }
}


@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
) = this.Title(stringResource(id))


@Composable
fun ListItemScope.Description(content: @Composable BoxScope.() -> Unit) {
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.outline
    )

    Box(modifier = Modifier.description()) {
        ProvideTextStyle(style) {
            content()
        }
    }
}


@Composable
fun ListItemScope.Description(text: String) {
    this.Description {
        Text(text.toStyleMarkup())
    }
}


@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
) = this.Description(stringResource(id))
