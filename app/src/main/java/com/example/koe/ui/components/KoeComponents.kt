package com.example.koe.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.theme.KoeTextStyles
import kotlin.math.roundToInt

internal enum class KoeButtonStyle { Primary, Secondary, Danger, Validate }

/** Pill button of the dialogs. Height comes from [modifier] (52 dp, or 44 dp in the shop). */
@Composable
internal fun KoeButton(
    text: String,
    onClick: () -> Unit,
    style: KoeButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes leadingIcon: Int? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val palette = style.palette(enabled)
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = palette.container,
        border = palette.border,
        modifier = modifier,
        enabled = enabled,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Image(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            KoeText(
                text = text,
                style = KoeTextStyles.Button,
                color = palette.content,
            )
            trailing?.invoke(this)
        }
    }
}

/** 44 dp round close button of the sheets: white, 3 dp ink ring, 20 dp cross. */
@Composable
internal fun KoeCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.cd_close)
    PressableSurface(
        onClick = onClick,
        shape = CircleShape,
        color = colorResource(R.color.koe_surface),
        border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
        modifier = modifier
            .size(44.dp)
            .semantics { contentDescription = description },
    ) {
        Image(
            painter = painterResource(R.drawable.ic_dialog_close),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Small rounded label: AU POUVOIR, CARTE POUVOIR, card kind badges. */
@Composable
internal fun KoeBadge(
    text: String,
    container: Color,
    content: Color,
    style: TextStyle,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    KoeText(
        text = text,
        style = style,
        color = content,
        modifier = modifier
            .background(container, RoundedCornerShape(10.dp))
            .padding(horizontal = horizontalPadding, vertical = 3.dp),
    )
}

private class KoeButtonPalette(
    val container: Color,
    val content: Color,
    val border: BorderStroke?,
)

@Composable
private fun KoeButtonStyle.palette(enabled: Boolean): KoeButtonPalette {
    if (!enabled) {
        return KoeButtonPalette(
            container = colorResource(R.color.koe_action_disabled),
            content = colorResource(R.color.koe_text_primary),
            border = null,
        )
    }
    return when (this) {
        KoeButtonStyle.Primary -> KoeButtonPalette(
            container = colorResource(R.color.koe_action_primary_container),
            content = colorResource(R.color.koe_action_primary_content),
            border = null,
        )
        KoeButtonStyle.Secondary -> KoeButtonPalette(
            container = colorResource(R.color.koe_action_secondary_container),
            content = colorResource(R.color.koe_action_secondary_content),
            border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
        )
        KoeButtonStyle.Danger -> KoeButtonPalette(
            container = colorResource(R.color.koe_action_danger),
            content = colorResource(R.color.koe_text_on_dark),
            border = null,
        )
        KoeButtonStyle.Validate -> KoeButtonPalette(
            container = colorResource(R.color.koe_action_validate_container),
            content = colorResource(R.color.koe_action_validate_content),
            border = null,
        )
    }
}

@Composable
internal fun AnimatedCount(
    value: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = value,
        modifier = modifier,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                (slideOutVertically { if (up) -it else it } + fadeOut()) using
                SizeTransform(clip = false)
        },
        contentAlignment = Alignment.CenterStart,
        label = "count",
    ) { count ->
        KoeText(
            text = count.toString(),
            style = style,
            color = color,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
internal fun PressableSurface(
    onClick: () -> Unit,
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    contentAlignment: Alignment = Alignment.Center,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(color, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
internal fun KoeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    // Written by onTextLayout during the text's own measure, read right after it by figmaLineBox.
    val lineCount = remember { intArrayOf(1) }
    BasicText(
        text = text,
        modifier = modifier.figmaLineBox(style.lineHeight) { lineCount[0] },
        style = style.merge(color = color, textAlign = textAlign ?: TextAlign.Unspecified),
        onTextLayout = { lineCount[0] = it.lineCount },
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
    )
}

/**
 * Sizes text to its Figma (CSS) line box, line count x line height, with the glyphs centred and
 * free to overflow it. Android grows the first and last line to the font's own ascent and descent
 * instead, which makes Anton 36/36 render 54 dp tall whatever the LineHeightStyle.
 */
internal fun Modifier.figmaLineBox(lineHeight: TextUnit, lineCount: () -> Int): Modifier =
    if (!lineHeight.isSp) {
        this
    } else {
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
            val box = (lineHeight.toPx() * lineCount()).roundToInt()
                .coerceIn(constraints.minHeight, constraints.maxHeight)
            layout(placeable.width, box) { placeable.place(0, (box - placeable.height) / 2) }
        }
    }
