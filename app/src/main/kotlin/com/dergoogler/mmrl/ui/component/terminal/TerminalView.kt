package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.Block
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

val LocalTerminalWidth = staticCompositionLocalOf<Dp> {
    error("CompositionLocal LocalTerminalWidth not present")
}

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
    val density = LocalDensity.current

    LaunchedEffect(list.size) {
        if (list.isNotEmpty()) {
            state.animateScrollToItem(list.size - 1)
        }
    }

    var lazyColumnWidth by remember { mutableIntStateOf(0) }

    ProvideTextStyle(value = style) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (userPrefs.terminalTextWrap) it else it.horizontalScroll(
                        rememberScrollState()
                    )
                }
        ) {
            SelectionContainer {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            lazyColumnWidth = coords.size.width
                        }
                        .then(modifier),
                    state = state,
                ) {
                    val widthDp = with(density) { lazyColumnWidth.toDp() }

                    itemsIndexed(list) { index, block ->
                        CompositionLocalProvider(
                            LocalTerminalWidth provides widthDp
                        ) {
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
    }
}

