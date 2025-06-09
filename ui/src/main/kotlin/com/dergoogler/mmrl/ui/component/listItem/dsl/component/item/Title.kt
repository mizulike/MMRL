package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope

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
