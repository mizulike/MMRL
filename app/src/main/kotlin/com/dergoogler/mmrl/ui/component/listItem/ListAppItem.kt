package com.dergoogler.mmrl.ui.component.listItem

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.dergoogler.mmrl.compat.content.AppInfo

@Composable
fun ListAppItem(
    modifier: Modifier = Modifier,
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    iconToRight: Boolean = false,
    enabled: Boolean = true,
    base: BaseParameters.() -> Unit = {},
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val start by remember {
        derivedStateOf {
            if (!iconToRight) contentPaddingValues.calculateStartPadding(
                layoutDirection
            ) else contentPaddingValues.calculateEndPadding(layoutDirection)
        }
    }

    val icon = app.icon

    val bitmap = remember(icon) {
        (icon as? BitmapDrawable)?.bitmap ?: icon.toBitmap()
    }
    val painter = remember(bitmap) { BitmapPainter(bitmap.asImageBitmap()) }

    Row(
        modifier = modifier
            .alpha(alpha = if (enabled) 1f else 0.5f)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .padding(contentPaddingValues)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!iconToRight) {
            Image(
                painter = painter,
                contentDescription = app.label,
                modifier = Modifier.size(itemTextStyle.iconSize)
            )

            Spacer(modifier = Modifier.width(start))
        }

        BaseListContent(
            modifier = Modifier.weight(1f),
            title = app.label,
            desc = app.packageName,
            itemTextStyle = itemTextStyle,
            base = base
        )

        if (iconToRight) {
            Spacer(modifier = Modifier.width(start))

            Image(
                painter = painter,
                contentDescription = app.label,
                modifier = Modifier.size(itemTextStyle.iconSize)
            )
        }
    }
}