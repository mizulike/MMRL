package com.dergoogler.mmrl.model.terminal

sealed interface Block {
    val lines: MutableList<Pair<Int, String>>
}