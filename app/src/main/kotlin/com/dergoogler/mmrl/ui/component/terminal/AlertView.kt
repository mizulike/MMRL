package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.AlertType
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun AlertView(block: AlertBlock) {
    val style = LocalTextStyle.current
    val color = when (block.type) {
        AlertType.NOTICE -> Color(0xff2294f2)
        AlertType.WARNING -> Color.Yellow
        AlertType.ERROR -> Color.Red
    }

    val textColor = when (block.type) {
        AlertType.NOTICE -> "#2294f2"
        AlertType.WARNING -> "yellow"
        AlertType.ERROR -> "red"
    }

    val type = block.title
        ?: when (block.type) {
            AlertType.NOTICE -> "Notice"
            AlertType.WARNING -> "Warning"
            AlertType.ERROR -> "Error"
        }

    val width = LocalTerminalWidth.current

    Line(
        modifier = Modifier
            .width(width)
            .background(color.copy(alpha = 0.09f)),
        index = block.lineNumber
    ) {
        BBCodeText(
            text = "[color=$textColor]$type:[/color] ${block.text}",
            style = style.copy(color = MaterialTheme.colorScheme.outline)
        )
    }
}
