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
    fun Modifier.layoutSlot(
        slot: ListItemSlot,
        disallow: List<ListItemSlot> = emptyList(),
    ): Modifier
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
    override fun Modifier.layoutSlot(
        slot: ListItemSlot,
        disallow: List<ListItemSlot>,
    ): Modifier {
        if (disallow.contains(slot)) {
            throw IllegalStateException("Slot $slot is not allowed in this scope")
        }

        return this.layoutId(slot)
    }
}