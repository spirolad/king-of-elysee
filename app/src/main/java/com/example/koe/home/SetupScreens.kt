package com.example.koe.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.match.Avatar
import com.example.koe.match.Candidate
import com.example.koe.match.game.toPlayer
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.components.PressableSurface
import com.example.koe.ui.theme.KoeTextStyles

/** 06 · Choix du personnage. */
@Composable
fun CharacterScreen(
    selected: Candidate,
    onSelect: (Candidate) -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = colorResource(R.color.koe_screen_home_content)
    Box(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .padding(rememberScreenPadding()),
    ) {
        // Centred above the thumbnails: on a phone this is Figma's 37.5 dp from the top, and the extra
        // height of a tablet goes equally above and below instead of under the quote.
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 78.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row {
                Box {
                    Avatar(
                        candidate = selected,
                        size = 196.dp,
                        border = BorderStroke(3.dp, colorResource(R.color.koe_surface)),
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_home_cocarde),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(x = 150.dp, y = (-6).dp)
                            .size(60.dp)
                            .rotate(12f),
                    )
                }
                Spacer(Modifier.width(36.dp))
                // Figma starts the text 8.5 dp above the portrait.
                Column(Modifier.offset(y = (-8.5).dp)) {
                    KoeText(stringResource(R.string.character_choose), KoeTextStyles.Lead, content)
                    Spacer(Modifier.height(26.dp))
                    KoeText(
                        text = stringResource(selected.nameRes).uppercase(),
                        style = KoeTextStyles.Hero,
                        color = content,
                        softWrap = false,
                    )
                    Spacer(Modifier.height(22.dp))
                    Column(
                        Modifier
                            .background(colorResource(R.color.koe_surface), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        KoeText(
                            text = stringResource(
                                R.string.character_quote_format,
                                stringResource(selected.quoteRes).trim('"', ' '),
                            ),
                            style = KoeTextStyles.BodyStrong,
                            color = colorResource(R.color.koe_text_primary),
                        )
                        Spacer(Modifier.height(4.dp))
                        KoeText(
                            text = stringResource(selected.taglineRes),
                            style = KoeTextStyles.Subtitle,
                            color = colorResource(R.color.koe_text_muted),
                        )
                    }
                }
            }
        }
        Row(
            Modifier.align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Candidate.entries.forEach { candidate ->
                CandidateTile(candidate, selected = candidate == selected, onClick = { onSelect(candidate) })
            }
        }
        KoeBigButton(
            text = stringResource(R.string.btn_validate_character),
            icon = R.drawable.ic_dialog_check,
            onClick = onValidate,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

/** 64 dp square portrait; the chosen one gets a 5 dp ink ring, the others a 2 dp white one. */
@Composable
private fun CandidateTile(candidate: Candidate, selected: Boolean, onClick: () -> Unit) {
    val name = stringResource(candidate.nameRes)
    val shape = RoundedCornerShape(16.dp)
    PressableSurface(
        onClick = onClick,
        shape = shape,
        color = colorResource(candidate.avatarColor),
        border = if (selected) {
            BorderStroke(5.dp, colorResource(R.color.koe_action_outline))
        } else {
            BorderStroke(2.dp, colorResource(R.color.koe_surface))
        },
        modifier = Modifier
            .size(64.dp)
            .semantics {
                contentDescription = name
                this.selected = selected
            },
    ) {
        val portrait = candidate.portrait
        if (portrait != null) {
            Image(
                painter = painterResource(portrait),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
            )
        } else {
            Avatar(candidate = candidate, size = 64.dp)
        }
    }
}

/** 07 · Prêts ?: the drawn play order, the human marked TOI. */
@Composable
fun ReadyScreen(
    order: List<Candidate>,
    human: Candidate,
    onBack: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val start = remember(human) { human.toPlayer() }
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_home_container))
            .padding(rememberScreenPadding()),
    ) {
        KoeScreenHeader(
            title = stringResource(R.string.ready_title),
            subtitle = stringResource(R.string.ready_subtitle),
            onBack = onBack,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            order.forEachIndexed { index, candidate ->
                PlayerCard(rank = index + 1, candidate = candidate, isHuman = candidate == human)
            }
        }
        Spacer(Modifier.height(17.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Row(
                Modifier
                    .weight(1f)
                    .padding(bottom = 21.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StartStat(
                    R.drawable.ic_board_stat_vote,
                    pluralStringResource(R.plurals.ready_start_votes, start.victoryPoints, start.victoryPoints),
                )
                StartStat(
                    R.drawable.ic_board_stat_popularite,
                    stringResource(R.string.ready_start_popularite, start.health),
                )
                StartStat(R.drawable.ic_board_stat_fonds, stringResource(R.string.ready_start_fonds, start.energy))
            }
            KoeBigButton(
                text = stringResource(R.string.btn_start_match),
                icon = R.drawable.ic_home_play,
                onClick = onStart,
            )
        }
    }
}

@Composable
private fun RowScope.PlayerCard(rank: Int, candidate: Candidate, isHuman: Boolean) {
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(colorResource(R.color.koe_surface_player_card), RoundedCornerShape(24.dp)),
    ) {
        Box(
            Modifier
                .padding(12.dp)
                .size(32.dp)
                .background(colorResource(R.color.koe_action_primary_container), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            KoeText(
                text = rank.toString(),
                style = KoeTextStyles.Rank,
                color = colorResource(R.color.koe_action_primary_content),
                softWrap = false,
            )
        }
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(candidate = candidate, size = 96.dp)
            Spacer(Modifier.height(12.dp))
            KoeText(
                text = stringResource(candidate.nameRes).uppercase(),
                style = KoeTextStyles.SheetTitle,
                color = colorResource(R.color.koe_text_primary),
                softWrap = false,
            )
            Spacer(Modifier.height(7.dp))
            KoeText(
                text = stringResource(if (isHuman) R.string.ready_you else R.string.ready_ai),
                style = KoeTextStyles.Caption,
                color = colorResource(R.color.koe_text_muted),
                softWrap = false,
            )
        }
    }
}

@Composable
private fun StartStat(icon: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
        KoeText(text, KoeTextStyles.StatLabelLarge, colorResource(R.color.koe_screen_home_content), softWrap = false)
    }
}

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun CharacterScreenPreview() = CharacterScreen(selected = Candidate.MACRON, onSelect = {}, onValidate = {})

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun ReadyScreenPreview() = ReadyScreen(
    order = listOf(Candidate.MELENCHON, Candidate.MACRON, Candidate.POUTOU, Candidate.LEPEN),
    human = Candidate.MACRON,
    onBack = {},
    onStart = {},
)
