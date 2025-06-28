package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class AddMask : Command {
    override val name: String = "add-mask"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            action.data.takeIf { it.isNotBlank() }?.let { masks += it }
        }
    }
}

class ReplaceSelf : Command {
    override val name: String = "replace-self"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            val key = action.getProp<String>("key")
            if (key.isNullOrBlank()) return

            action.data.takeIf { it.isNotBlank() }?.let { data ->
                val index = console.indexOfFirst { it is TextBlock && it.key == key }

                if (index >= 0) {
                    val oldBlock = console[index] as TextBlock
                    val newBlock = TextBlock(oldBlock.lineNumber, data.fixNewLines, key = key)
                    if (oldBlock != newBlock) {
                        console[index] = newBlock
                    }
                    lineAdded = false // replaced existing line → no new line added
                } else {
                    console += TextBlock(lineNumber, data.fixNewLines, key = key)
                    lineAdded = true  // new line added
                }
            } ?: run {
                lineAdded = false // no data, no new line
            }
        }
    }
}
