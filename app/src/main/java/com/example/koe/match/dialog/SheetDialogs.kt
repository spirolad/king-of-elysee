package com.example.koe.match.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.match.Avatar
import com.example.koe.ui.components.KoeBadge
import com.example.koe.ui.components.KoeButton
import com.example.koe.ui.components.KoeButtonStyle
import com.example.koe.ui.components.KoeCloseButton
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.theme.KoeTextStyles

private val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

// Mechancete tiers of the MY CARDS meter.
private val MechanceteTierIndexes = setOf(2, 5, 9)

@Composable
internal fun ShopDialog(
    dialog: MatchDialog.Shop,
    actions: MatchDialogActions,
) {
    var selectedId by rememberSaveable {
        mutableStateOf(if (dialog.canBuy) dialog.cards.firstOrNull { it.enabled && !it.owned }?.id else null)
    }
    val selected = if (dialog.canBuy) dialog.cards.firstOrNull { it.id == selectedId } else null
    val remaining = selected?.cost?.let { dialog.fonds - it }
    val affordable = remaining != null && remaining >= 0

    Column(
        Modifier
            .size(750.dp, 311.dp)
            .background(colorResource(R.color.koe_surface), SheetShape)
            .testTag(MatchDialogTags.SHOP),
    ) {
        SheetHeader(
            icon = R.drawable.ic_board_shop,
            title = stringResource(R.string.shop_title),
            hint = stringResource(R.string.shop_hint),
            actions = actions,
            trailing = {
                Image(
                    painter = painterResource(R.drawable.ic_board_fonds),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(4.dp))
                KoeText(
                    text = dialog.fonds.toString(),
                    style = KoeTextStyles.Value,
                    color = colorResource(R.color.koe_text_warning),
                )
                Spacer(Modifier.width(17.dp))
            },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                Modifier
                    .width(474.dp)
                    .height(206.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dialog.cards.forEach { card ->
                    PowerCard(
                        card = card,
                        selected = card.id == selectedId,
                        onClick = {
                            if (dialog.canBuy && card.enabled && !card.owned) {
                                selectedId = card.id
                            }
                        },
                        onLongClick = { actions.onCardDetails(card) },
                    )
                }
            }
            Column(Modifier.width(208.dp)) {
                Column(
                    Modifier
                        .testTag(MatchDialogTags.CHOICE)
                        .fillMaxWidth()
                        .height(98.dp)
                        .background(colorResource(R.color.koe_surface_inset), RoundedCornerShape(16.dp))
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp),
                ) {
                    KoeText(
                        text = stringResource(R.string.shop_your_choice),
                        style = KoeTextStyles.Caption,
                        color = colorResource(R.color.koe_text_muted),
                    )
                    Spacer(Modifier.height(4.dp))
                    if (!dialog.canBuy) {
                        KoeText(
                            text = stringResource(R.string.shop_locked_hint),
                            style = KoeTextStyles.StatLabelLarge,
                            color = colorResource(R.color.koe_text_muted),
                            modifier = Modifier.testTag(MatchDialogTags.SHOP_LOCKED_HINT),
                        )
                    } else if (selected != null) {
                        KoeText(
                            text = selected.title,
                            style = KoeTextStyles.BodyStrong,
                            color = colorResource(R.color.koe_text_primary),
                        )
                        Spacer(Modifier.height(2.dp))
                        if (remaining != null && remaining >= 0) {
                            KoeText(
                                text = stringResource(R.string.shop_affordable, remaining),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_success),
                            )
                        } else {
                            KoeText(
                                text = stringResource(R.string.shop_unaffordable),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_danger),
                            )
                        }
                    } else {
                        KoeText(
                            text = stringResource(R.string.shop_no_choice),
                            style = KoeTextStyles.StatLabelLarge,
                            color = colorResource(R.color.koe_text_muted),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                KoeButton(
                    text = stringResource(R.string.btn_buy),
                    onClick = { selected?.let(actions.onBuy) },
                    style = KoeButtonStyle.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = affordable,
                )
                Spacer(Modifier.height(10.dp))
                KoeButton(
                    text = stringResource(R.string.btn_renew),
                    onClick = actions.onRenew,
                    style = KoeButtonStyle.Secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = dialog.canBuy,
                    trailing = {
                        Image(
                            painter = painterResource(R.drawable.ic_board_fonds),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        KoeText(
                            text = dialog.renewCost.toString(),
                            style = KoeTextStyles.ValueSpaced,
                            color = colorResource(R.color.koe_text_warning),
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun MyCardsDialog(
    dialog: MatchDialog.MyCards,
    actions: MatchDialogActions,
) {
    Column(
        Modifier
            .size(750.dp, 304.dp)
            .background(colorResource(R.color.koe_surface), SheetShape)
            .testTag(MatchDialogTags.MY_CARDS),
    ) {
        SheetHeader(
            icon = R.drawable.ic_board_cards,
            title = stringResource(R.string.my_cards_title),
            hint = stringResource(R.string.my_cards_hint),
            actions = actions,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                Modifier
                    .width(474.dp)
                    .height(208.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dialog.cards.forEach { card ->
                    PowerCard(
                        card = card,
                        onLongClick = { actions.onCardDetails(card) },
                    )
                }
            }
            Column(
                Modifier
                    .testTag(MatchDialogTags.MECHANCETE_PANEL)
                    .size(208.dp, 206.dp)
                    .background(colorResource(R.color.koe_surface_mechancete), RoundedCornerShape(24.dp))
                    .padding(start = 14.dp, top = 14.dp, end = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_board_stat_mechancete),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    KoeText(
                        text = stringResource(R.string.stat_mechancete),
                        style = KoeTextStyles.BodyStrong,
                        color = colorResource(R.color.koe_text_primary),
                    )
                }
                Spacer(Modifier.height(7.5.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    KoeText(
                        text = dialog.mechancete.toString(),
                        style = KoeTextStyles.DieNumber,
                        color = colorResource(R.color.koe_text_mechancete),
                    )
                    Spacer(Modifier.width(5.dp))
                    KoeText(
                        text = stringResource(R.string.mechancete_progress, dialog.nextTier),
                        style = KoeTextStyles.StatLabelLarge,
                        color = colorResource(R.color.koe_text_mechancete),
                        modifier = Modifier.offset(y = 2.5.dp),
                    )
                }
                Spacer(Modifier.height(9.5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(10) { index ->
                        val filled = index < dialog.mechancete
                        val segmentShape = RoundedCornerShape(4.dp)
                        Box(
                            Modifier
                                .size(15.3.dp, 12.dp)
                                .background(
                                    if (filled) {
                                        colorResource(R.color.koe_meter_mechancete_on)
                                    } else {
                                        colorResource(R.color.koe_surface)
                                    },
                                    segmentShape,
                                )
                                .then(
                                    if (!filled && index in MechanceteTierIndexes) {
                                        Modifier.border(2.dp, colorResource(R.color.koe_text_mechancete), segmentShape)
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {}
                    }
                }
                Spacer(Modifier.height(8.dp))
                KoeText(
                    text = stringResource(R.string.mechancete_tiers_hint),
                    style = KoeTextStyles.Description,
                    color = colorResource(R.color.koe_text_mechancete),
                )
            }
        }
    }
}

@Composable
internal fun HistoryDialog(
    dialog: MatchDialog.History,
    actions: MatchDialogActions,
) {
    Column(
        Modifier
            .size(750.dp, 304.dp)
            .background(colorResource(R.color.koe_surface), SheetShape)
            .testTag(MatchDialogTags.HISTORY),
    ) {
        SheetHeader(
            icon = R.drawable.ic_dialog_clock,
            title = stringResource(R.string.history_title),
            actions = actions,
        )
        Spacer(Modifier.height(12.dp))
        if (dialog.entries.isEmpty()) {
            KoeText(
                text = stringResource(R.string.history_empty),
                style = KoeTextStyles.Body,
                color = colorResource(R.color.koe_text_muted),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        } else {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(dialog.entries) { entry ->
                    HistoryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntryUi) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .background(colorResource(entry.actor.avatarColor), CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        KoeText(
            text = historySentence(entry),
            style = KoeTextStyles.Body,
            color = colorResource(R.color.koe_text_primary),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        KoeText(
            text = stringResource(R.string.history_round, entry.round),
            style = KoeTextStyles.Caption,
            color = colorResource(R.color.koe_text_muted),
        )
    }
}

@Composable
private fun historySentence(entry: HistoryEntryUi): String {
    val actor = stringResource(entry.actor.nameRes)
    val target = entry.target?.let { stringResource(it.nameRes) }.orEmpty()
    val card = entry.cardName.orEmpty()
    val amount = entry.amount ?: 0
    return when (entry.kind) {
        HistoryKind.TURN_STARTED -> stringResource(R.string.history_turn_started, actor)
        HistoryKind.TURN_SKIPPED -> stringResource(R.string.history_turn_skipped, actor)
        HistoryKind.TWEET -> stringResource(R.string.history_tweet, actor, target, amount)
        HistoryKind.ENTERED_ELYSEE -> stringResource(R.string.history_entered, actor)
        HistoryKind.YIELDED_ELYSEE -> stringResource(R.string.history_yielded, actor)
        HistoryKind.BOUGHT_CARD -> stringResource(R.string.history_bought, actor, card, amount)
        HistoryKind.MECHANCETE_CARD -> stringResource(R.string.history_mechancete, actor, card)
        HistoryKind.ELIMINATED -> stringResource(R.string.history_eliminated, actor)
    }
}

@Composable
internal fun PlayerDetailsDialog(
    dialog: MatchDialog.PlayerDetails,
    actions: MatchDialogActions,
) {
    val name = stringResource(dialog.candidate.nameRes).uppercase()
    val votesFraction = (dialog.votes / 20f).coerceIn(0f, 1f)
    Box(
        Modifier
            .size(810.dp, 354.dp)
            .background(colorResource(R.color.koe_surface), SheetShape)
            .testTag(MatchDialogTags.PLAYER),
    ) {
        Row {
            Column(
                Modifier
                    .padding(start = 14.dp, top = 24.dp)
                    .width(121.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Avatar(
                        candidate = dialog.candidate,
                        size = 75.dp,
                        border = if (dialog.isKing) {
                            BorderStroke(3.dp, colorResource(R.color.koe_status_highlight))
                        } else {
                            null
                        },
                    )
                    if (dialog.isKing) {
                        Image(
                            painter = painterResource(R.drawable.ic_dialog_crown),
                            contentDescription = null,
                            modifier = Modifier
                                .offset(x = 44.dp, y = (-12.4).dp)
                                .size(36.dp)
                                .rotate(14f),
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KoeText(
                        text = name,
                        style = KoeTextStyles.PlayerName,
                        color = colorResource(R.color.koe_text_primary),
                        // Figma lets a long name overflow the 121 dp column, centred, instead of clipping it.
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                    )
                    if (dialog.isKing) {
                        KoeBadge(
                            text = stringResource(R.string.match_badge_in_power),
                            container = colorResource(R.color.koe_status_highlight),
                            content = colorResource(R.color.koe_text_primary),
                            style = KoeTextStyles.CardBadge,
                            horizontalPadding = 8.dp,
                        )
                    }
                    KoeText(
                        text = stringResource(R.string.player_details_number, dialog.playerNumber),
                        style = KoeTextStyles.Subtitle,
                        color = colorResource(R.color.koe_text_muted),
                    )
                }
            }
            Spacer(Modifier.width(25.dp))
            Column(Modifier.padding(top = 20.dp)) {
                Row(
                    Modifier
                        .testTag(MatchDialogTags.STATS)
                        .width(577.dp)
                        .background(colorResource(R.color.koe_surface_inset), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_dialog_popularite),
                                contentDescription = null,
                                modifier = Modifier.size(15.3455.dp, 14.105.dp),
                            )
                            KoeText(
                                text = dialog.popularite.toString(),
                                style = KoeTextStyles.DieNumber,
                                color = colorResource(R.color.koe_text_popularite),
                            )
                            KoeText(
                                text = stringResource(R.string.stat_popularite_max),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_muted),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(10) { index ->
                                Box(
                                    Modifier
                                        .size(16.dp, 8.dp)
                                        .background(
                                            if (index < dialog.popularite) {
                                                colorResource(R.color.koe_meter_popularite_on)
                                            } else {
                                                colorResource(R.color.koe_meter_popularite_off)
                                            },
                                            RoundedCornerShape(4.dp),
                                        ),
                                ) {}
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_dialog_vote),
                                contentDescription = null,
                                modifier = Modifier.size(16.5.dp, 16.958.dp),
                            )
                            KoeText(
                                text = dialog.votes.toString(),
                                style = KoeTextStyles.DieNumber,
                                color = colorResource(R.color.koe_text_votes),
                            )
                            KoeText(
                                text = stringResource(R.string.stat_votes_max),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_muted),
                            )
                        }
                        Box(
                            Modifier
                                .size(176.dp, 8.dp)
                                .background(colorResource(R.color.koe_meter_votes_off), RoundedCornerShape(4.dp)),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(votesFraction)
                                    .background(colorResource(R.color.koe_meter_votes_on), RoundedCornerShape(4.dp)),
                            ) {}
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_board_fonds),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            KoeText(
                                text = dialog.fonds.toString(),
                                style = KoeTextStyles.Value,
                                color = colorResource(R.color.koe_text_warning),
                            )
                            KoeText(
                                text = stringResource(R.string.stat_fonds),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_muted),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_board_stat_mechancete),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            KoeText(
                                text = dialog.mechancete.toString(),
                                style = KoeTextStyles.Value,
                                color = colorResource(R.color.koe_text_mechancete),
                            )
                            KoeText(
                                text = stringResource(R.string.stat_mechancete),
                                style = KoeTextStyles.StatLabelLarge,
                                color = colorResource(R.color.koe_text_muted),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(13.dp))
                KoeText(
                    text = stringResource(R.string.player_details_cards),
                    style = KoeTextStyles.SectionTitle,
                    color = colorResource(R.color.koe_text_primary),
                    modifier = Modifier.padding(start = 1.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .width(634.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dialog.cards.forEach { card ->
                        PowerCard(
                            card = card,
                            onLongClick = { actions.onCardDetails(card) },
                        )
                    }
                }
            }
        }
        KoeCloseButton(
            onClick = actions.onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .testTag(MatchDialogTags.CLOSE),
        )
    }
}

@Composable
private fun SheetHeader(
    icon: Int,
    title: String,
    hint: String? = null,
    actions: MatchDialogActions,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 18.dp, end = 24.dp)
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        KoeText(
            text = title,
            style = KoeTextStyles.SheetTitle,
            color = colorResource(R.color.koe_text_primary),
        )
        Spacer(Modifier.width(16.dp))
        if (hint != null) {
            KoeText(
                text = hint,
                style = KoeTextStyles.Subtitle,
                color = colorResource(R.color.koe_text_muted),
                maxLines = 1,
            )
        }
        Spacer(Modifier.weight(1f))
        trailing()
        KoeCloseButton(
            onClick = actions.onDismiss,
            modifier = Modifier.testTag(MatchDialogTags.CLOSE),
        )
    }
}
