package com.dergoogler.mmrl.model.terminal

data class GroupBlock(
    val title: String? = null,
    val startLine: Int? = null,
    override val lines: MutableList<Pair<Int, String>> = mutableListOf(),
    val initiallyExpanded: Boolean = true,
) : Block
