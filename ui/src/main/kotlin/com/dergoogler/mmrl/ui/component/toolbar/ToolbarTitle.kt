package com.dergoogler.mmrl.ui.component.toolbar

import androidx.annotation.StringRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.dergoogler.mmrl.ext.nullable

/**
 * Displays a title and an optional subtitle in a toolbar.
 *
 * This composable provides a convenient way to display localized title and subtitle text in a toolbar.
 * It takes string resource IDs as input and handles the string resolution internally.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param title The string resource ID for the main title.
 * @param subtitle The optional string resource ID for the subtitle. If null, no subtitle is displayed.
 *
 * @sample
 * ```kotlin
 *  ToolbarTitle(
 *    title = R.string.my_app_name,
 *    subtitle = R.string.my_app_subtitle
 * )
 * ```
 *
 * @sample
 * ```kotlin
 * ToolbarTitle(
 *     title = R.string.only_title
 * )
 * ```
 */
@Composable
fun ToolbarTitle(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
) = ToolbarTitle(
    title = stringResource(title),
    subtitle = subtitle.nullable { stringResource(it) },
    modifier = modifier,
)

/**
 * Displays a title and an optional subtitle in a layout suitable for a toolbar.
 *
 * This composable provides a convenient way to render a primary title with an optional secondary
 * subtitle, typically used within a toolbar or header area. It automatically applies appropriate
 * text styles and handles text truncation for long titles or subtitles.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param title The main title text to display.
 * @param subtitle An optional subtitle text to display below the title. If null, no subtitle will be shown.
 *
 * @see ToolbarTitleLayout
 */
@Composable
fun ToolbarTitle(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
) = ToolbarTitleLayout(
    modifier = modifier,
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LocalContentColor.current
        )
    },
    subtitle = subtitle.nullable {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = LocalContentColor.current.copy(alpha = 0.75f)
            )
        }
    }
)

@Composable
internal fun ToolbarTitleLayout(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
) {
    val measurePolicy = remember { ToolbarTitleMeasurePolicy() }

    Layout(
        contents = listOf(
            title,
            subtitle ?: {}
        ),
        modifier = modifier,
        measurePolicy = measurePolicy,
    )
}

private class ToolbarTitleMeasurePolicy : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<List<Measurable>>,
        constraints: Constraints,
    ): MeasureResult {
        val titleMeasurable = measurables.getOrNull(0)?.firstOrNull()
        val subtitleMeasurable = measurables.getOrNull(1)?.firstOrNull()

        val titlePlaceable = titleMeasurable?.measure(constraints)
        val subtitlePlaceable = subtitleMeasurable?.measure(constraints)

        val width = listOfNotNull(titlePlaceable?.width, subtitlePlaceable?.width).maxOrNull() ?: 0
        val height = listOfNotNull(titlePlaceable?.height, subtitlePlaceable?.height).sum()

        return layout(width, height) {
            var y = 0
            titlePlaceable?.placeRelative(0, y)
            y += titlePlaceable?.height ?: 0
            subtitlePlaceable?.placeRelative(0, y)
        }
    }
}
