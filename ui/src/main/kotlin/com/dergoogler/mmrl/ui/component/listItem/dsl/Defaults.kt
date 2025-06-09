package com.dergoogler.mmrl.ui.component.listItem.dsl

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp

enum class ListItemSlot {
    Start,
    End,
    Title,
    Description
}

@LayoutScopeMarker
@Immutable
interface BaseListScope {
    val contentPaddingValues: PaddingValues
    val iconSize: Dp
}

@LayoutScopeMarker
@Immutable
interface ListItemScope : BaseListScope {
    fun Modifier.title(): Modifier
    fun Modifier.description(): Modifier
    fun Modifier.start(): Modifier
    fun Modifier.end(): Modifier
}

@LayoutScopeMarker
@Immutable
interface ListScope : BaseListScope

internal class ListScopeInstance(
    override val contentPaddingValues: PaddingValues,
    override val iconSize: Dp,
) : ListScope

internal class ListItemScopeInstance(
    override val contentPaddingValues: PaddingValues,
    override val iconSize: Dp,
) : ListItemScope {
    override fun Modifier.title(): Modifier = layoutId(ListItemSlot.Title)
    override fun Modifier.description(): Modifier = layoutId(ListItemSlot.Description)
    override fun Modifier.start(): Modifier = layoutId(ListItemSlot.Start)
    override fun Modifier.end(): Modifier = layoutId(ListItemSlot.End)
}