package com.example.koe.match.dialog

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.koe.R
import com.example.koe.ui.components.KoeBadge
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.components.PressableSurface
import com.example.koe.ui.theme.KoeTextStyles

@Composable
internal fun PowerCard(
    card: PowerCardUi,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    PressableSurface(
        // Always enabled so long press still works on disabled cards.
        onClick = { if (card.enabled) onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        color = colorResource(R.color.koe_surface),
        border = if (selected) {
            BorderStroke(3.dp, colorResource(R.color.koe_status_highlight))
        } else {
            BorderStroke(2.dp, colorResource(R.color.koe_surface_line))
        },
        contentAlignment = Alignment.TopStart,
        onLongClick = onLongClick,
        modifier = modifier
            .size(150.dp, 200.dp)
            .alpha(if (card.enabled) 1f else 0.5f)
            .testTag(MatchDialogTags.card(card.id))
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PowerCardHeader(card = card)
            when (card.kind) {
                CardKind.POUVOIR, CardKind.ACTION -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KoeText(
                        text = card.title,
                        style = KoeTextStyles.CardTitle,
                        color = colorResource(R.color.koe_text_primary),
                        textAlign = TextAlign.Center,
                    )
                    KoeText(
                        text = card.description,
                        style = KoeTextStyles.Description,
                        color = colorResource(R.color.koe_text_muted),
                        modifier = Modifier.width(124.dp),
                        textAlign = TextAlign.Start,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CardKind.MECHANCETE -> {
                    KoeText(
                        text = card.title,
                        style = KoeTextStyles.CardTitle,
                        color = colorResource(R.color.koe_text_primary),
                        textAlign = TextAlign.Center,
                    )
                    KoeText(
                        text = card.description,
                        style = KoeTextStyles.Description,
                        color = colorResource(R.color.koe_text_muted),
                        modifier = Modifier.width(137.dp),
                        textAlign = TextAlign.Start,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            PowerCardFooter(kind = card.kind)
        }
    }
}

@Composable
private fun PowerCardHeader(card: PowerCardUi) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colorResource(card.kind.headerColorRes())),
        contentAlignment = Alignment.Center,
    ) {
        when (card.kind) {
            CardKind.POUVOIR -> Image(
                painter = painterResource(R.drawable.ic_dialog_star),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            CardKind.ACTION -> Image(
                painter = painterResource(R.drawable.ic_dialog_bolt),
                contentDescription = null,
                modifier = Modifier.size(17.dp, 23.dp),
            )
            CardKind.MECHANCETE -> Image(
                painter = painterResource(R.drawable.ic_board_stat_mechancete),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        }
        val cost = card.cost
        if (cost != null) {
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 6.dp)
                    .size(41.dp, 24.dp)
                    .background(colorResource(R.color.koe_surface), RoundedCornerShape(12.dp))
                    .padding(start = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_board_fonds),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(3.dp))
                KoeText(
                    text = cost.toString(),
                    style = KoeTextStyles.ChipStat,
                    color = colorResource(R.color.koe_text_primary),
                )
            }
        }
        if (card.owned) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 8.dp)
                    .size(26.dp)
                    .background(colorResource(R.color.koe_card_owned), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_dialog_check),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PowerCardFooter(kind: CardKind) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        KoeBadge(
            text = stringResource(kind.labelRes()),
            container = colorResource(kind.badgeContainerRes()),
            content = colorResource(kind.badgeContentRes()),
            style = KoeTextStyles.CardBadge,
            horizontalPadding = 6.dp,
        )
    }
}

@ColorRes
private fun CardKind.headerColorRes(): Int = when (this) {
    CardKind.POUVOIR -> R.color.koe_card_header_pouvoir
    CardKind.ACTION -> R.color.koe_card_header_action
    CardKind.MECHANCETE -> R.color.koe_card_header_mechancete
}

@StringRes
private fun CardKind.labelRes(): Int = when (this) {
    CardKind.POUVOIR -> R.string.card_type_pouvoir
    CardKind.ACTION -> R.string.card_type_action
    CardKind.MECHANCETE -> R.string.card_type_mechancete
}

@ColorRes
private fun CardKind.badgeContainerRes(): Int = when (this) {
    CardKind.POUVOIR -> R.color.koe_status_highlight
    CardKind.ACTION -> R.color.koe_card_badge_action
    CardKind.MECHANCETE -> R.color.koe_card_badge_mechancete
}

@ColorRes
private fun CardKind.badgeContentRes(): Int = when (this) {
    CardKind.POUVOIR -> R.color.koe_text_primary
    CardKind.ACTION -> R.color.koe_text_on_dark
    CardKind.MECHANCETE -> R.color.koe_text_on_dark
}
