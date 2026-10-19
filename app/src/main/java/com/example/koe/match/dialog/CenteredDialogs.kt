package com.example.koe.match.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.match.Avatar
import com.example.koe.ui.components.KoeBadge
import com.example.koe.ui.components.KoeButton
import com.example.koe.ui.components.KoeButtonStyle
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.theme.KoeTextStyles

@Composable
internal fun PauseDialog(
    dialog: MatchDialog.Pause,
    actions: MatchDialogActions,
) {
    Row(
        Modifier
            .size(560.dp, 286.dp)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(32.dp))
            .testTag(MatchDialogTags.PAUSE)
            .padding(start = 24.dp, top = 24.dp, end = 24.dp),
    ) {
        Column(Modifier.width(278.dp)) {
            KoeText(
                text = stringResource(R.string.pause_title),
                style = KoeTextStyles.DialogTitle,
                color = colorResource(R.color.koe_text_primary),
            )
            Spacer(Modifier.height(26.dp))
            KoeText(
                text = stringResource(
                    R.string.pause_turn,
                    dialog.turn,
                    stringResource(dialog.currentPlayer.nameRes),
                ),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_primary),
            )
            Spacer(Modifier.height(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(colorResource(R.color.koe_surface_inset), RoundedCornerShape(16.dp))
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dialog.ranking.forEach { row ->
                    Row(
                        Modifier.height(20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            Modifier
                                .size(20.dp)
                                .background(colorResource(row.candidate.avatarColor), CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        KoeText(
                            text = stringResource(row.candidate.nameRes),
                            style = KoeTextStyles.Name,
                            color = colorResource(R.color.koe_text_primary),
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 1.dp),
                        )
                        KoeText(
                            text = row.votes.toString(),
                            style = KoeTextStyles.ChipStat,
                            color = colorResource(R.color.koe_text_votes),
                            modifier = Modifier.padding(top = 1.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_board_vote),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(
            Modifier.width(210.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KoeButton(
                text = stringResource(R.string.btn_resume),
                onClick = actions.onResume,
                style = KoeButtonStyle.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                leadingIcon = R.drawable.ic_dialog_play,
            )
            KoeButton(
                text = stringResource(R.string.btn_rules),
                onClick = actions.onRules,
                style = KoeButtonStyle.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )
            KoeButton(
                text = stringResource(R.string.btn_settings),
                onClick = actions.onSettings,
                style = KoeButtonStyle.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )
            KoeButton(
                text = stringResource(R.string.btn_quit),
                onClick = actions.onQuitRequest,
                style = KoeButtonStyle.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                leadingIcon = R.drawable.ic_dialog_logout,
            )
        }
    }
}

@Composable
internal fun QuitConfirmDialog(
    dialog: MatchDialog.QuitConfirm,
    actions: MatchDialogActions,
) {
    Box(
        Modifier
            .size(460.dp, 212.dp)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(32.dp))
            .testTag(MatchDialogTags.QUIT),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 28.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KoeText(
                text = stringResource(R.string.quit_title),
                style = KoeTextStyles.Display,
                color = colorResource(R.color.koe_text_primary),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            KoeText(
                text = stringResource(
                    R.string.quit_message,
                    stringResource(dialog.king.nameRes),
                ),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_muted),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KoeButton(
                    text = stringResource(R.string.btn_cancel),
                    onClick = actions.onDismiss,
                    style = KoeButtonStyle.Secondary,
                    modifier = Modifier.size(200.dp, 52.dp),
                )
                KoeButton(
                    text = stringResource(R.string.btn_quit),
                    onClick = actions.onQuitConfirmed,
                    style = KoeButtonStyle.Danger,
                    modifier = Modifier.size(200.dp, 52.dp),
                )
            }
        }
        // Drawn last so the bonnet sits over the panel edge; the panel itself does not clip.
        Image(
            painter = painterResource(R.drawable.ic_dialog_bonnet),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (-22).dp, y = (-26).dp)
                .size(70.dp, 62.dp)
                .rotate(-12f),
        )
    }
}

@Composable
internal fun YieldElyseeDialog(
    dialog: MatchDialog.YieldElysee,
    actions: MatchDialogActions,
) {
    val kingName = stringResource(dialog.king.nameRes)
    val attackerName = stringResource(dialog.attacker.nameRes)
    Column(
        Modifier
            .size(560.dp, 308.dp)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(32.dp))
            .testTag(MatchDialogTags.YIELD)
            .padding(start = 24.dp, top = 24.dp, end = 24.dp),
    ) {
        Row(Modifier.height(72.dp)) {
            Box {
                Avatar(
                    candidate = dialog.king,
                    size = 72.dp,
                    border = BorderStroke(3.dp, colorResource(R.color.koe_status_highlight)),
                )
                Image(
                    painter = painterResource(R.drawable.ic_dialog_crown),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = 50.dp, y = (-12).dp)
                        .size(30.dp)
                        .rotate(14f),
                )
            }
            Spacer(Modifier.width(23.dp))
            Column {
                KoeBadge(
                    text = stringResource(R.string.yield_pass_phone, kingName.uppercase()),
                    container = colorResource(R.color.koe_action_primary_container),
                    content = colorResource(R.color.koe_text_on_dark),
                    style = KoeTextStyles.Caption,
                    horizontalPadding = 8.dp,
                )
                Spacer(Modifier.height(16.dp))
                KoeText(
                    text = stringResource(R.string.yield_title),
                    style = KoeTextStyles.DialogTitleSmall,
                    color = colorResource(R.color.koe_text_primary),
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(R.color.koe_surface_inset), RoundedCornerShape(16.dp))
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_board_tweet),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(10.dp))
            KoeText(
                text = stringResource(R.string.yield_tweet_notice, attackerName),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_primary),
            )
            Spacer(Modifier.width(12.dp))
            KoeText(
                text = stringResource(R.string.yield_damage, dialog.popularityLoss),
                style = KoeTextStyles.BodyStrong,
                color = colorResource(R.color.koe_text_danger),
            )
            Spacer(Modifier.width(4.dp))
            KoeText(
                text = stringResource(R.string.yield_remaining, dialog.popularityLeft),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_primary),
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                Modifier.width(250.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KoeButton(
                    text = stringResource(R.string.btn_stay),
                    onClick = actions.onStay,
                    style = KoeButtonStyle.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                )
                KoeText(
                    text = stringResource(R.string.yield_stay_hint),
                    style = KoeTextStyles.Subtitle,
                    color = colorResource(R.color.koe_text_muted),
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                Modifier.width(250.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KoeButton(
                    text = stringResource(R.string.btn_yield),
                    onClick = actions.onYield,
                    style = KoeButtonStyle.Secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                )
                KoeText(
                    text = stringResource(R.string.yield_cede_hint, attackerName),
                    style = KoeTextStyles.Subtitle,
                    color = colorResource(R.color.koe_text_muted),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun MechanceteDialog(
    dialog: MatchDialog.Mechancete,
    actions: MatchDialogActions,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = dialog.choices.firstOrNull { it.id == selectedId }
    Box(
        Modifier
            .size(680.dp, 320.dp)
            .background(colorResource(R.color.koe_screen_evil_container), RoundedCornerShape(32.dp))
            .testTag(MatchDialogTags.MECHANCETE),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, top = 20.dp, end = 24.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    KoeText(
                        text = stringResource(R.string.mechancete_title),
                        style = KoeTextStyles.Display,
                        color = colorResource(R.color.koe_screen_evil_content),
                    )
                    KoeText(
                        text = stringResource(R.string.mechancete_reached, dialog.level),
                        style = KoeTextStyles.Body,
                        color = colorResource(R.color.koe_screen_evil_content),
                    )
                }
                KoeButton(
                    text = stringResource(R.string.btn_validate),
                    onClick = { selected?.let(actions.onMechanceteValidate) },
                    style = KoeButtonStyle.Validate,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(125.3.dp, 52.dp),
                    enabled = selectedId != null,
                )
            }
            Spacer(Modifier.height(17.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                dialog.choices.forEach { card ->
                    PowerCard(
                        card = card,
                        selected = card.id == selectedId,
                        onClick = { selectedId = card.id },
                        onLongClick = { actions.onCardDetails(card) },
                    )
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_dialog_mechancete_badge),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (-24).dp, y = (-26).dp)
                .size(72.dp)
                .rotate(-14f),
        )
    }
}

@Composable
internal fun CardDetailDialog(
    dialog: MatchDialog.CardDetail,
    actions: MatchDialogActions,
) {
    val card = dialog.card
    val labelRes = when (card.kind) {
        CardKind.POUVOIR -> R.string.card_detail_badge_pouvoir
        CardKind.ACTION -> R.string.card_detail_badge_action
        CardKind.MECHANCETE -> R.string.card_detail_badge_mechancete
    }
    val badgeContainerRes = when (card.kind) {
        CardKind.POUVOIR -> R.color.koe_status_highlight
        CardKind.ACTION -> R.color.koe_card_badge_action
        CardKind.MECHANCETE -> R.color.koe_card_badge_mechancete
    }
    val badgeContentRes = when (card.kind) {
        CardKind.POUVOIR -> R.color.koe_text_primary
        CardKind.ACTION -> R.color.koe_text_on_dark
        CardKind.MECHANCETE -> R.color.koe_text_on_dark
    }
    Box(
        Modifier
            .size(644.dp, 306.dp)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(32.dp))
            .testTag(MatchDialogTags.CARD_DETAIL),
    ) {
        PowerCard(
            card = card,
            modifier = Modifier
                .offset(x = 44.33.dp, y = 42.dp)
                .rotate(-15f),
        )
        Column(
            Modifier
                .padding(start = 250.dp, top = 30.81.dp)
                .width(330.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KoeBadge(
                text = stringResource(labelRes),
                container = colorResource(badgeContainerRes),
                content = colorResource(badgeContentRes),
                style = KoeTextStyles.Caption,
                horizontalPadding = 8.dp,
            )
            KoeText(
                text = card.title.uppercase(),
                style = KoeTextStyles.DialogTitle,
                color = colorResource(R.color.koe_text_primary),
                maxLines = 1,
            )
            KoeText(
                text = card.description,
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_primary),
            )
            if (card.paidLine != null || card.kind == CardKind.POUVOIR) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.koe_surface_inset), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    card.paidLine?.let { paidLine ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_board_fonds),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            KoeText(
                                text = paidLine,
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_primary),
                            )
                        }
                    }
                    if (card.kind == CardKind.POUVOIR) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_dialog_clock),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            KoeText(
                                text = stringResource(R.string.card_detail_permanent),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_primary),
                            )
                        }
                    }
                }
            }
        }
        KoeButton(
            text = stringResource(R.string.btn_ok),
            onClick = actions.onDismiss,
            style = KoeButtonStyle.Primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 20.dp)
                .size(80.6.dp, 52.dp),
        )
    }
}
