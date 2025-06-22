package com.dergoogler.mmrl.model.terminal

enum class AlertType {
    NOTICE,
    WARNING,
    ERROR
}

data class AlertBlock(
    val lineNumber: Int,
    val type: AlertType,
    val text: String,
    override val lines: MutableList<Pair<Int, String>> = mutableListOf(),
) : Block