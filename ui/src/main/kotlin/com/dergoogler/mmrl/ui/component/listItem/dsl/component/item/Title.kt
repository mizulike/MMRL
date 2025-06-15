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
) = Title {
    val titleStyle = LocalTitleStyle.current
    val style = LocalTextStyle.current.merge(LocalTypography.current.fromToken(titleStyle))

    ProvideTextStyle(style) {
        Text(text)
    }
}

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
) = Title(stringResource(id))

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
    vararg formatArgs: Any,
) = Title(stringResource(id, formatArgs))

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
