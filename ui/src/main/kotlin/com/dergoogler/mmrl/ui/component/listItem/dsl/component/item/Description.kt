package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

/**
 * A description for the list item.
 *
 * This composable function is used to display a description within a list item.
 * It applies a specific text style (bodyMedium with outline color) to the content.
 *
 * @param content A composable lambda that defines the content of the description.
 *                This lambda is executed within a [BoxScope], allowing for flexible layout options.
 */
@Composable
fun ListItemScope.Description(
    content: @Composable BoxScope.() -> Unit,
) {
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.outline
    )

    Slot(
        slot = ListItemSlot.Description,
        content = {
            ProvideTextStyle(style) {
                content()
            }
        }
    )
}


/**
 * A description for the list item.
 *
 * @param text The description text. Supports style markup.
 */
@Composable
fun ListItemScope.Description(text: String) {
    this.Description {
        Text(text.toStyleMarkup())
    }
}


/**
 * A description for the list item.
 *
 * @param id The string resource ID for the description text.
 */
@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
) = this.Description(stringResource(id))


/**
 * A description for the list item.
 *
 * @param id The string resource ID for the description text.
 * @param formatArgs The format arguments for the string resource.
 */
@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    vararg formatArgs: Any,
) = this.Description(stringResource(id, *formatArgs))

@Composable
fun ListItemScope.Description(
    text: String,
    labels: List<@Composable () -> Unit>,
) = Description {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text)

        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                labels.forEach { label ->
                    label()
                }
            }
        }
    }
}

@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    labels: List<@Composable () -> Unit>,
) = Description(
    text = stringResource(id),
    labels = labels
)

@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    vararg formatArgs: Any,
    labels: List<@Composable () -> Unit>,
) = Description(
    text = stringResource(id, *formatArgs),
    labels = labels
)

@Composable
fun ListItemScope.Description(
    text: String,
    @StringRes learnMoreText: Int = R.string.learn_more,
    onLearnMore: () -> Unit,
) = Description {
    val currentStyle = LocalTextStyle.current
    val style = currentStyle.copy(color = MaterialTheme.colorScheme.primary)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text)

        Text(
            modifier = Modifier
                .padding(top = 5.dp)
                .clickable(
                    onClick = onLearnMore
                ),
            text = stringResource(learnMoreText),
            style = style,
        )
    }
}

@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    @StringRes learnMoreText: Int = R.string.learn_more,
    onLearnMore: () -> Unit,
) = Description(
    text = stringResource(id),
    learnMoreText = learnMoreText,
    onLearnMore = onLearnMore
)

@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    @StringRes learnMoreText: Int = R.string.learn_more,
    vararg formatArgs: Any,
    onLearnMore: () -> Unit,
) = Description(
    text = stringResource(id, *formatArgs),
    learnMoreText = learnMoreText,
    onLearnMore = onLearnMore
)

