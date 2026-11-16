package com.example.koe.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.components.KoeAdaptiveFrame
import com.example.koe.ui.components.KoeButton
import com.example.koe.ui.components.KoeButtonStyle
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.theme.KoeTextStyles

/** 08 · Passe le téléphone, shown before each of the human's turns; a tap anywhere starts the turn. */
@Composable
fun TurnIntro(
    candidate: Candidate,
    intro: TurnIntroUi,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = colorResource(R.color.koe_screen_board_content)
    // Scaled up on tablets; the whole screen, letterbox included, starts the turn.
    KoeAdaptiveFrame(
        background = colorResource(R.color.koe_screen_board_container),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onStart,
        ),
    ) { TurnIntroContent(candidate, intro, onStart, content) }
}

@Composable
private fun TurnIntroContent(
    candidate: Candidate,
    intro: TurnIntroUi,
    onStart: () -> Unit,
    content: Color,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            KoeText(
                text = stringResource(R.string.turn_intro_badge, intro.round),
                style = KoeTextStyles.Caption,
                color = colorResource(R.color.koe_text_primary),
                softWrap = false,
                modifier = Modifier
                    .background(colorResource(R.color.koe_surface), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(20.dp))
            NameBanner(candidate)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(30.dp), verticalAlignment = Alignment.CenterVertically) {
                IntroStat(
                    R.drawable.ic_board_stat_vote,
                    pluralStringResource(R.plurals.turn_intro_votes, intro.votes, intro.votes),
                )
                IntroStat(R.drawable.ic_board_stat_popularite, stringResource(R.string.turn_intro_popularite, intro.popularite))
                IntroStat(R.drawable.ic_board_stat_fonds, stringResource(R.string.turn_intro_fonds, intro.fonds))
            }
            Spacer(Modifier.height(21.dp))
            KoeText(stringResource(R.string.turn_intro_hint), KoeTextStyles.Body, content)
            Spacer(Modifier.height(20.dp))
            KoeButton(
                text = stringResource(R.string.btn_lets_go),
                onClick = onStart,
                style = KoeButtonStyle.Secondary,
                modifier = Modifier
                    .height(52.dp)
                    .width(194.dp),
                trailing = {
                    Image(
                        painter = painterResource(R.drawable.ic_home_arrow_next),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
    }
}

/** Ink pill with the portrait spilling over its start and the bonnet over its end. */
@Composable
private fun NameBanner(candidate: Candidate) {
    Box {
        Box(
            Modifier
                .padding(top = 22.dp, start = 10.dp)
                .widthIn(min = 640.dp)
                .height(116.dp)
                .background(colorResource(R.color.koe_action_primary_container), RoundedCornerShape(58.dp))
                .padding(start = 160.dp, end = 48.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            KoeText(
                text = stringResource(R.string.turn_intro_title, stringResource(candidate.nameRes).uppercase()),
                style = KoeTextStyles.Hero,
                color = colorResource(R.color.koe_action_primary_content),
                softWrap = false,
            )
        }
        Avatar(
            candidate = candidate,
            size = 156.dp,
            border = BorderStroke(3.dp, colorResource(R.color.koe_surface)),
        )
        Image(
            painter = painterResource(R.drawable.ic_dialog_bonnet),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-16).dp)
                .size(76.dp, 66.dp)
                .rotate(14f),
        )
    }
}

@Composable
private fun IntroStat(icon: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
        KoeText(text, KoeTextStyles.Value, colorResource(R.color.koe_screen_board_content), softWrap = false)
    }
}

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun TurnIntroPreview() = TurnIntro(
    candidate = Candidate.MACRON,
    intro = TurnIntroUi(round = 9, votes = 14, popularite = 7, fonds = 5),
    onStart = {},
)
