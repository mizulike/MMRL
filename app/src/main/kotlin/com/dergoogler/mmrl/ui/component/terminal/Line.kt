package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun Line(
    modifier: Modifier = Modifier,
    index: Int?,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LineNumber(index)
        content()
    }
}

@Composable
fun RowScope.LineNumber(number: Int?) {
    val userPrefs = LocalUserPreferences.current
    if (!userPrefs.showTerminalLineNumbers) return

    val index = remember(number) {
        if (number == null || number == -1) "" else number.toString()
    }

    val style = LocalTextStyle.current

    DisableSelection {
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = index,
                style = style.copy(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.6f
                    ),
                )
            )
        }
    }
}