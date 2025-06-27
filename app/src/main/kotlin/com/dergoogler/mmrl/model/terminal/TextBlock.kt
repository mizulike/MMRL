package com.dergoogler.mmrl.model.terminal

data class TextBlock(
    val lineNumber: Int,
    val text: String,
    override val lines: MutableList<Pair<Int, String>> = mutableListOf(),
    val key: String? = null
) : Block