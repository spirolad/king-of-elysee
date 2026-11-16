package com.example.koe.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.components.KoeButton
import com.example.koe.ui.components.KoeButtonStyle
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.components.PressableSurface
import com.example.koe.ui.theme.KoeTextStyles

// Figma frames are 844 x 390; stickers are placed from the frame's centre so they follow the logo.
private const val FRAME_CENTER_X = 422f
private const val FRAME_CENTER_Y = 195f

/** 01 · Écran d'accueil: the whole screen starts the game. */
@Composable
fun TitleScreen(onStart: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onStart,
            ),
    ) {
        Sticker(R.drawable.ic_dialog_bonnet, 86.dp, 76.dp, -14f, left = 147f + 8f, top = 94.8f + 11f)
        Sticker(R.drawable.ic_home_cocarde, 72.dp, 72.dp, 10f, left = 594f + 6f, top = 42.8f + 7f)
        Sticker(R.drawable.ic_home_flag, 84.dp, 70.dp, 8f, left = 604f + 4.7f, top = 156.8f + 7.5f)
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(bottom = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KoeLogo()
            Spacer(Modifier.height(7.dp))
            KoeText(
                text = stringResource(R.string.title_tagline),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_screen_home_content),
            )
            Spacer(Modifier.height(15.dp))
            PressableSurface(
                onClick = onStart,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                color = colorResource(R.color.koe_action_primary_container),
                modifier = Modifier.height(44.dp),
            ) {
                Row(
                    Modifier.padding(start = 22.dp, end = 38.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(R.drawable.ic_home_play), null, Modifier.size(20.dp))
                    KoeText(
                        text = stringResource(R.string.title_tap_to_start),
                        style = KoeTextStyles.Button,
                        color = colorResource(R.color.koe_action_primary_content),
                        softWrap = false,
                    )
                }
            }
        }
    }
}

/** A tilted decoration whose unrotated box sits at [left], [top] in the Figma frame. */
@Composable
private fun BoxScope.Sticker(
    @DrawableRes icon: Int,
    width: Dp,
    height: Dp,
    degrees: Float,
    left: Float,
    top: Float,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(
                x = (left + width.value / 2 - FRAME_CENTER_X).dp,
                y = (top + height.value / 2 - FRAME_CENTER_Y).dp,
            )
            .size(width, height)
            .rotate(degrees),
    )
}

/** 02 · Menu principal. */
@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sounds by rememberSetting(KoeSetting.SOUNDS)
    Row(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .padding(horizontal = 80.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.padding(bottom = 18.dp)) {
            KoeLogo(horizontalAlignment = Alignment.Start)
            Spacer(Modifier.height(7.dp))
            KoeText(
                text = stringResource(R.string.menu_tagline),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_screen_home_content),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .width(300.dp),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KoeBigButton(
                text = stringResource(R.string.btn_play),
                icon = R.drawable.ic_home_play,
                onClick = onPlay,
                modifier = Modifier.width(280.dp),
            )
            Spacer(Modifier.height(12.dp))
            KoeButton(
                text = stringResource(R.string.btn_rules),
                onClick = onRules,
                style = KoeButtonStyle.Secondary,
                leadingIcon = R.drawable.ic_home_rules,
                iconSize = 22.dp,
                modifier = Modifier
                    .width(280.dp)
                    .height(52.dp),
            )
            Spacer(Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KoeRoundButton(
                    icon = R.drawable.ic_home_settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    onClick = onSettings,
                )
                KoeRoundButton(
                    icon = if (sounds) R.drawable.ic_home_sound else R.drawable.ic_home_sound_off,
                    contentDescription = stringResource(
                        if (sounds) R.string.cd_sound_off else R.string.cd_sound_on,
                    ),
                    onClick = { sounds = !sounds },
                )
                // No game history is kept yet, so the trophy is shown but does nothing.
                KoeRoundButton(
                    icon = R.drawable.ic_home_trophy,
                    contentDescription = stringResource(R.string.cd_last_games),
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun TitleScreenPreview() = TitleScreen(onStart = {})

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun MainMenuScreenPreview() = MainMenuScreen(onPlay = {}, onRules = {}, onSettings = {})
