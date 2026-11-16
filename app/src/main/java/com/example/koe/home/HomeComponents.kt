package com.example.koe.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.components.PressableSurface
import com.example.koe.ui.theme.KoeTextStyles

/**
 * Padding of the setup screens: Figma's 47 dp side edge, which already clears most camera cutouts,
 * grown when a cutout is deeper than that.
 */
@Composable
internal fun rememberScreenPadding(top: Dp = 20.dp, bottom: Dp = 21.dp): PaddingValues {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val cutout = WindowInsets.displayCutout
    val edge = dimensionResource(R.dimen.koe_board_edge)
    val start = with(density) { maxOf(edge, cutout.getLeft(density, direction).toDp()) }
    val end = with(density) { maxOf(edge, cutout.getRight(density, direction).toDp()) }
    return PaddingValues(start = start, top = top, end = end, bottom = bottom)
}

/** KING OF in white outline over ÉLYSÉE in ink, as on the title and menu screens. */
@Composable
internal fun KoeLogo(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val stroke = with(LocalDensity.current) { 1.5.dp.toPx() }
    Column(modifier, horizontalAlignment = horizontalAlignment) {
        KoeText(
            text = stringResource(R.string.logo_king_of),
            style = KoeTextStyles.LogoKingOf.copy(drawStyle = Stroke(width = stroke)),
            color = colorResource(R.color.koe_text_on_dark),
            softWrap = false,
            // Figma sets KING OF 5 dp right of ÉLYSÉE's left edge.
            modifier = if (horizontalAlignment == Alignment.Start) Modifier.offset(x = 5.dp) else Modifier,
        )
        // The two words overlap slightly in Figma.
        KoeText(
            text = stringResource(R.string.logo_elysee),
            style = KoeTextStyles.LogoElysee,
            color = colorResource(R.color.koe_screen_home_content),
            softWrap = false,
            modifier = Modifier.offset(y = (-4).dp),
        )
    }
}

/** 52 dp round button: white, 3 dp ink ring, 24 dp icon. */
@Composable
internal fun KoeRoundButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    PressableSurface(
        onClick = onClick,
        shape = CircleShape,
        color = colorResource(R.color.koe_surface),
        border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
        enabled = enabled,
        modifier = modifier
            .size(52.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

/** 64 dp ink pill with an Anton label: JOUER, VALIDER, COMMENCER. */
@Composable
internal fun KoeBigButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = colorResource(R.color.koe_action_primary_container),
        modifier = modifier.height(64.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
            KoeText(
                text = text,
                style = KoeTextStyles.ButtonLarge,
                color = colorResource(R.color.koe_action_primary_content),
                softWrap = false,
            )
        }
    }
}

/** Back button and Anton title of the inner screens, with an optional action at the end. */
@Composable
internal fun KoeScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        KoeRoundButton(
            icon = R.drawable.ic_home_back,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBack,
        )
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            KoeText(
                text = title,
                style = if (subtitle == null) KoeTextStyles.Display else KoeTextStyles.PlayerName,
                color = colorResource(R.color.koe_screen_home_content),
                softWrap = false,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                KoeText(
                    text = subtitle,
                    style = KoeTextStyles.LeadSmall,
                    color = colorResource(R.color.koe_screen_home_content),
                )
            }
        }
        action?.invoke(this)
    }
}
