package com.dergoogler.mmrl.ui.component.text

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.toStyleMarkup

/**
 * A composable function that displays text with BBCode formatting.
 *
 * BBCode (Bulletin Board Code) is a lightweight markup language used to format posts in many
 * message boards. This function parses and renders common BBCode tags like [b], [i], [u],
 * [s], [color], [url], etc.
 *
 * It extends the functionality of the standard `Text` composable by providing support for BBCode.
 *
 * @param text The string containing BBCode to be displayed.
 * @param modifier The modifier to be applied to the layout.
 * @param color The color of the text. `Color.Unspecified` uses the color from the [style].
 * @param fontSize The size of the font. `TextUnit.Unspecified` uses the font size from the [style].
 * @param fontStyle The font style (e.g., italic). `null` uses the font style from the [style].
 * @param fontWeight The font weight (e.g., bold). `null` uses the font weight from the [style].
 * @param fontFamily The font family. `null` uses the font family from the [style].
 * @param letterSpacing The spacing between characters. `TextUnit.Unspecified` uses the letter spacing from the [style].
 * @param textDecoration The decoration to apply to the text (e.g., underline, line-through). `null` uses the text decoration from the [style].
 * @param textAlign The alignment of the text within its container. `null` uses the text alignment from the [style].
 * @param lineHeight The height of each line of text. `TextUnit.Unspecified` uses the line height from the [style].
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should break at soft line breaks. If false, the glyphs in the
 * text are positioned as if there is unlimited horizontal space. If `softWrap` is false, `overflow`
 * and `textAlign` may have unexpected effects.
 * @param maxLines An optional maximum number of lines for the text to span, wrapping if necessary.
 * If the text exceeds the given number of lines, it will be truncated according to `overflow`.
 * If it is not null, then `softWrap` must be true.
 */
@Composable
fun BBCodeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    onLinkClick: ((String) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val handler = LocalUriHandler.current
    val currentColor = LocalContentColor.current
    val textColor by remember(color) {
        derivedStateOf {
            color.takeOrElse { style.color.takeOrElse { currentColor } }
        }
    }

    var layoutResult: TextLayoutResult? by remember { mutableStateOf(null) }

    val annotatedString = text.toStyleMarkup()

    BasicText(
        text = annotatedString,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    layoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)

                        val ann = annotatedString
                            .getStringAnnotations(
                                tag = "URL", start = position, end = position
                            )
                            .firstOrNull()

                        ann.nullable {
                            if (onLinkClick != null) {
                                onLinkClick(it.item)
                                return@nullable
                            }

                            handler.openUri(it.item)
                        }
                    }
                }
            }
            .then(modifier),
        style = style.merge(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        ),
        onTextLayout = {
            layoutResult = it
            onTextLayout.nullable { it2 ->
                it2(it)
            }
        },
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines
    )
}