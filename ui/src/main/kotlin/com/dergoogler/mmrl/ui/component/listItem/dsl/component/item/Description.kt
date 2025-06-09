package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope

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
