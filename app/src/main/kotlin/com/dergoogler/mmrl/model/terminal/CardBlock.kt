package com.dergoogler.mmrl.model.terminal

data class CardBlock(
    override val lines: MutableList<Pair<Int, String>> = mutableListOf(),
) : Block
