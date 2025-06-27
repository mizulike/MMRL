package com.dergoogler.mmrl.model.terminal.commands

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