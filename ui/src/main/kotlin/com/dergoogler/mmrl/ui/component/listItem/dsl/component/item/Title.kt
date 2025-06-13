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
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

/**
 * Title component used in a list item.
 *
 * It is typically used to display the main text of the list item.
 *
 * @param content The composable content to be displayed as the title.
 */
@Composable
fun ListItemScope.Title(
    content: @Composable BoxScope.() -> Unit,
) {
    val style = MaterialTheme.typography.bodyLarge.copy(
        color = LocalContentColor.current
    )

    Slot(
        slot = ListItemSlot.Title,
        content = {
            ProvideTextStyle(style) {
                content()
            }

        }
    )
}

/**
 * A title that can be used in a [ListItemScope].
 * This composable is a shorthand for [Title] with a [Text] as content.
 *
 * @param text The text to display.
 */
@Composable
fun ListItemScope.Title(
    text: String,
) {
    this.Title {
        Text(text)
    }
}

/**
 * Composable function to display a title within a ListItem.
 * This overload takes a string resource ID and resolves it to a string.
 *
 * @param id The string resource ID for the title text.
 */
@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
) = this.Title(stringResource(id))
