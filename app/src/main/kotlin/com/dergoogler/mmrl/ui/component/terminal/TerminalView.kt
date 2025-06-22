package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.Block
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun TerminalView(
    modifier: Modifier = Modifier,
    list: SnapshotStateList<Block>,
    state: LazyListState,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.Monospace
    ),
) {
    val userPrefs = LocalUserPreferences.current

    LaunchedEffect(list.size) {
        if (list.isNotEmpty()) {
            state.animateScrollToItem(list.size - 1)
        }
    }

    ProvideTextStyle(value = style) {
        SelectionContainer {
            LazyColumn(
                modifier = modifier.let {
                    if (userPrefs.terminalTextWrap) it else it.horizontalScroll(rememberScrollState())
                },
                state = state,
            ) {
                itemsIndexed(list) { index, block ->
                    when (block) {
                        is GroupBlock -> GroupBlockView(block)
                        is CardBlock -> CardView(block)
                        is TextBlock -> TextView(block)
                        is AlertBlock -> AlertView(block)
                    }
                }
            }
        }
    }
}

