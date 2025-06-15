package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.token.LocalTypography
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens
import com.dergoogler.mmrl.ui.token.fromToken
import com.dergoogler.mmrl.ui.token.value

@Composable
fun ListItemScope.Title(
    content: @Composable BoxScope.() -> Unit,
) {
    ProvideTitleTypography(LocalTitleStyle.current) {
        Slot(
            slot = ListItemSlot.Title,
            content = content
        )
    }
}

@Composable
fun ListItemScope.Title(
    text: String,
    style: TextStyle = LocalTextStyle.current,
) = Title {
    val titleStyle = LocalTitleStyle.current
    val textStyle = LocalTypography.current.fromToken(titleStyle).merge(style)

    ProvideTextStyle(textStyle) {
        Text(text)
    }
}

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
    style: TextStyle = LocalTextStyle.current,
) = Title(stringResource(id), style)

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
    style: TextStyle = LocalTextStyle.current,
    vararg formatArgs: Any,
) = Title(stringResource(id, formatArgs), style)

val LocalTitleStyle = staticCompositionLocalOf {
    TypographyKeyTokens.BodyLarge
}

@Composable
fun ProvideTitleTypography(
    token: TypographyKeyTokens,
    content: @Composable () -> Unit,
) {
    val typography = token.value
    val mergedStyle = LocalTextStyle.current.merge(typography)
    CompositionLocalProvider(
        LocalTextStyle provides mergedStyle,
        LocalTitleStyle provides token,
        content = content
    )
}
