package com.dergoogler.mmrl.ui.component.listItem.dsl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val DefaultContentPaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp)
val DefaultIconSize = 24.dp

@Composable
fun List(
    modifier: Modifier = Modifier,
    contentPaddingValues: PaddingValues = DefaultContentPaddingValues,
    content: @Composable ListScope.() -> Unit,
) {
    val instance = remember {
        ListScopeInstance(
            contentPaddingValues = contentPaddingValues,
            iconSize = DefaultIconSize
        )
    }

    Column(modifier = modifier) {
        instance.content()
    }
}