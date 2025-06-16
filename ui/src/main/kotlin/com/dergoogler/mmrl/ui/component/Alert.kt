package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.toStyleMarkup
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.text.TextWithIcon
import com.dergoogler.mmrl.ui.component.text.TextWithIconDefaults

@Composable
fun Alert(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = contentColorFor(backgroundColor),
    title: String?,
    message: String,
    @DrawableRes icon: Int? = null,
) = Card(
    modifier = modifier
        .padding(vertical = 16.dp, horizontal = 25.dp)
        .fillMaxWidth(),
    color = backgroundColor,
    contentColor = textColor,
) {
    Column(
        modifier = Modifier.relative(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title.nullable {
            TextWithIcon(
                text = it,
                icon = icon,
                style = TextWithIconDefaults.style.copy(
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    ),
                    spacing = 8.dp,
                    iconTint = textColor
                )
            )
        }

        Text(
            text = message.toStyleMarkup(),
            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
        )
    }
}