package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

@Composable
fun ListItemScope.LearnMore(
    @StringRes text: Int = R.string.learn_more,
    onLearnMore: () -> Unit,
) {
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.primary
    )

    ProvideTextStyle(style) {
        Slot(ListItemSlot.Supporting) {
            Text(
                modifier = Modifier.clickable(
                    onClick = onLearnMore
                ),
                text = stringResource(text)
            )
        }
    }
}