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

@Composable
fun TopAppBarTitle(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes subtitle: Int? = null,
) = TopAppBarTitle(
    title = stringResource(title),
    subtitle = subtitle.nullable { stringResource(it) },
    modifier = modifier,
)

@Composable
fun TopAppBarTitle(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
) = TopAppBarTitleLayout(
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
private fun TopAppBarTitleLayout(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
) {
    val measurePolicy = remember { TopAppBarTitleMeasurePolicy() }

    Layout(
        contents = listOf(
            title,
            subtitle ?: {}
        ),
        modifier = modifier,
        measurePolicy = measurePolicy,
    )
}

private class TopAppBarTitleMeasurePolicy : MultiContentMeasurePolicy {
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
