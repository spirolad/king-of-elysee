package com.example.koe.match

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.koe.R
import com.example.koe.ui.components.AnimatedCount
import com.example.koe.ui.components.KoeText
import com.example.koe.ui.components.PressableSurface
import com.example.koe.ui.theme.KoeTextStyles
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

object MatchBoardTags {
    const val PAUSE = "match:pause"
    const val KING_PANEL = "match:kingPanel"
    const val ME_PANEL = "match:mePanel"
    const val DICE_ROW = "match:diceRow"
    const val RESULT = "match:result"
    const val SHOP = "match:shop"
    const val CARDS = "match:cards"
    const val HISTORY = "match:history"
    const val END_TURN = "match:endTurn"
    const val ROLL = "match:roll"
    const val VALIDATE_DICE = "match:validateDice"
    const val VOTES_DELTA = "match:votesDelta"
    const val TWEET_BANNER = "match:tweetBanner"
    const val STEP_BAR = "match:stepBar"
    const val SHOP_LOCK = "match:shopLock"
    const val SHOP_AFFORDABLE = "match:shopAffordable"
    const val DICE_LEGEND_BUTTON = "match:diceLegendButton"
    const val DICE_LEGEND = "match:diceLegend"
    const val ELYSEE_BANNER = "match:elyseeBanner"
    fun chip(candidate: Candidate) = "match:chip:${candidate.name}"
    fun die(index: Int) = "match:die:$index"
}

/** Figma screen edges: 47 dp sides (camera cutout included), 12 top, 21 bottom. */
val MatchBoardDefaultPadding = PaddingValues(start = 47.dp, top = 12.dp, end = 47.dp, bottom = 21.dp)

@Composable
fun MatchBoard(
    state: MatchUiState,
    onPauseClick: () -> Unit,
    onOpponentClick: (Candidate) -> Unit,
    onShopClick: () -> Unit,
    onCardsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEndTurnClick: () -> Unit,
    onDieClick: (Int) -> Unit,
    onRollClick: () -> Unit,
    onValidateDiceClick: () -> Unit,
    onElyseeBannerDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MatchBoardDefaultPadding,
) {
    val step = turnStepOf(state)
    val pulseReady = rememberPulseReady(step = step, rollNumber = state.roll?.rollNumber)
    val shopState = when (state.shopAvailability) {
        ShopAvailability.LOCKED -> stringResource(R.string.match_shop_locked)
        ShopAvailability.AFFORDABLE -> stringResource(R.string.match_shop_affordable)
        ShopAvailability.OPEN -> null
    }
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(R.color.koe_screen_board_container))
            .padding(contentPadding),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .zIndex(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PauseButton(onClick = onPauseClick)
            Spacer(Modifier.width(BoardMetrics.Gap))
            Box(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BoardMetrics.Gap),
                ) {
                    state.opponents.forEach { opponent ->
                        OpponentChip(
                            opponent = opponent,
                            onClick = { onOpponentClick(opponent.candidate) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                TweetBanner(
                    banner = state.tweetBanner,
                    targetIndex = state.opponents.indexOfFirst { it.candidate == state.tweetBanner?.target },
                    count = state.opponents.size,
                )
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            KingPanel(king = state.king)
            CenterColumn(
                state = state,
                step = step,
                onDieClick = onDieClick,
                onElyseeBannerDismiss = onElyseeBannerDismiss,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 18.dp),
            )
            MePanel(
                me = state.me,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        StepBar(step = step)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            SecondaryButton(
                onClick = onShopClick,
                icon = R.drawable.ic_board_shop,
                label = R.string.btn_shop,
                tag = MatchBoardTags.SHOP,
                stateText = shopState,
                badge = { ShopBadge(availability = state.shopAvailability) },
            )
            Spacer(Modifier.width(BoardMetrics.Gap))
            SecondaryButton(
                onClick = onCardsClick,
                icon = R.drawable.ic_board_cards,
                label = R.string.btn_cards,
                tag = MatchBoardTags.CARDS,
                badgeCount = state.cardsCount,
            )
            Spacer(Modifier.width(BoardMetrics.Gap))
            SecondaryButton(
                onClick = onHistoryClick,
                icon = R.drawable.ic_dialog_clock,
                label = R.string.btn_history,
                tag = MatchBoardTags.HISTORY,
            )
            Spacer(Modifier.weight(1f))
            val roll = state.roll
            if (roll != null && roll.rollNumber >= 1) {
                SecondaryButton(
                    onClick = onValidateDiceClick,
                    icon = null,
                    label = R.string.btn_validate,
                    tag = MatchBoardTags.VALIDATE_DICE,
                    pulse = pulseReady && step == TurnStep.KEEP,
                )
                Spacer(Modifier.width(BoardMetrics.Gap))
            }
            // The fixed box keeps the row height while the button is hidden.
            Box(Modifier.size(216.3.dp, 64.dp)) {
                if (roll != null) {
                    PrimaryPillButton(
                        label = R.string.btn_roll,
                        tag = MatchBoardTags.ROLL,
                        onClick = onRollClick,
                        pulse = pulseReady && step == TurnStep.ROLL,
                    )
                } else {
                    EndTurnButton(
                        visible = state.canEndTurn,
                        onClick = onEndTurnClick,
                        pulse = pulseReady && step == TurnStep.SHOP,
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseButton(onClick: () -> Unit) {
    val pauseDescription = stringResource(R.string.cd_pause)
    PressableSurface(
        onClick = onClick,
        shape = CircleShape,
        color = colorResource(R.color.koe_surface),
        border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
        modifier = Modifier
            .size(52.dp)
            .testTag(MatchBoardTags.PAUSE)
            .semantics { contentDescription = pauseDescription },
    ) {
        Image(
            painter = painterResource(R.drawable.ic_board_pause),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun OpponentChip(
    opponent: OpponentUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(opponent.candidate.nameRes)
    val description = if (opponent.isKing) {
        stringResource(R.string.match_chip_description_king, name, opponent.popularite, opponent.votes)
    } else {
        stringResource(R.string.match_chip_description, name, opponent.popularite, opponent.votes)
    }
    // Eliminated wins over playing: a player cannot be both in practice.
    val chipState = when {
        opponent.isEliminated -> stringResource(R.string.match_chip_eliminated)
        opponent.isPlaying -> stringResource(R.string.match_chip_playing)
        else -> null
    }
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = colorResource(R.color.koe_surface),
        // The playing border wins over the king's so the turn is visible; the king stays in the description.
        border = when {
            opponent.isEliminated -> null
            opponent.isPlaying -> BorderStroke(3.dp, colorResource(R.color.koe_action_focus))
            opponent.isKing -> BorderStroke(3.dp, colorResource(R.color.koe_status_highlight))
            else -> null
        },
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .height(56.dp)
            .alpha(if (opponent.isEliminated) 0.4f else 1f)
            .testTag(MatchBoardTags.chip(opponent.candidate))
            .semantics(mergeDescendants = true) {
                contentDescription = description
                chipState?.let { stateDescription = it }
            },
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 6.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Avatar(
                candidate = opponent.candidate,
                size = 44.dp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.padding(top = 9.dp)) {
                KoeText(
                    text = name,
                    style = KoeTextStyles.Name,
                    color = colorResource(R.color.koe_text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.Top) {
                    Image(
                        painter = painterResource(R.drawable.ic_board_popularite),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(16.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    AnimatedCount(
                        value = opponent.popularite,
                        style = KoeTextStyles.ChipStat,
                        color = colorResource(R.color.koe_text_popularite),
                        modifier = Modifier.widthIn(min = 20.dp),
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_board_vote),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(16.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    AnimatedCount(
                        value = opponent.votes,
                        style = KoeTextStyles.ChipStat,
                        color = colorResource(R.color.koe_text_votes),
                    )
                }
            }
        }
        if (opponent.isKing) {
            Image(
                painter = painterResource(R.drawable.ic_board_crown),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 11.dp, end = 18.4.dp)
                    .size(13.dp, 9.dp),
            )
        }
    }
}

@Composable
private fun KingPanel(king: Candidate?) {
    Column(
        Modifier
            .testTag(MatchBoardTags.KING_PANEL)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(32.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        KoeText(
            text = stringResource(R.string.match_king_title),
            style = KoeTextStyles.KingTitle,
            color = colorResource(R.color.koe_text_primary),
            textAlign = TextAlign.Center,
        )
        if (king != null) {
            Avatar(
                candidate = king,
                size = 64.dp,
                border = BorderStroke(3.dp, colorResource(R.color.koe_status_highlight)),
            )
        } else {
            // Empty seat: same footprint as the avatar, outlined instead of filled.
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.koe_surface_sunken))
                    .border(BorderStroke(3.dp, colorResource(R.color.koe_surface_line)), CircleShape),
            )
        }
        KoeText(
            text = if (king != null) {
                stringResource(king.nameRes)
            } else {
                stringResource(R.string.match_king_empty)
            },
            style = KoeTextStyles.Name,
            color = colorResource(R.color.koe_text_primary),
            textAlign = TextAlign.Center,
        )
        if (king != null) {
            KoeText(
                text = stringResource(R.string.match_badge_in_power),
                style = KoeTextStyles.Caption,
                color = colorResource(R.color.koe_text_primary),
                modifier = Modifier
                    .background(colorResource(R.color.koe_status_highlight), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        KoeText(
            text = stringResource(R.string.match_king_bonus),
            style = KoeTextStyles.Description,
            color = colorResource(R.color.koe_text_muted),
            modifier = Modifier.width(124.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CenterColumn(
    state: MatchUiState,
    step: TurnStep?,
    onDieClick: (Int) -> Unit,
    onElyseeBannerDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val myName = stringResource(state.me.candidate.nameRes)
    val title = if (state.currentPlayer == state.me.candidate) {
        stringResource(R.string.match_your_turn, myName)
    } else {
        stringResource(R.string.match_their_turn, stringResource(state.currentPlayer.nameRes))
    }
    val roll = state.roll
    val kept = state.dice.count { it.highlighted }
    val keeping = step == TurnStep.KEEP
    val subtitle = when {
        roll == null -> stringResource(R.string.match_turn_result)
        roll.rollNumber == 0 -> stringResource(R.string.match_roll_prompt)
        keeping && kept > 0 -> pluralStringResource(
            R.plurals.match_roll_count_kept,
            kept,
            roll.rollNumber,
            roll.maxRolls,
            kept,
        )
        else -> stringResource(R.string.match_roll_count, roll.rollNumber, roll.maxRolls)
    }
    // Dice only lock in once the human has thrown at least once.
    val diceTappable = roll != null && roll.rollNumber >= 1
    // The box gives the Élysée banner an anchor on the column's top edge without adding to the column.
    Box(modifier) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                KoeText(
                    text = title.uppercase(),
                    style = KoeTextStyles.Display,
                    color = colorResource(R.color.koe_screen_board_content),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                VotesDelta(delta = state.votesDelta)
            }
            KoeText(
                text = subtitle,
                style = KoeTextStyles.Subtitle,
                color = colorResource(R.color.koe_screen_board_content),
                textAlign = TextAlign.Center,
            )
            DiceTray(
                dice = state.dice,
                tappable = diceTappable,
                keeping = keeping,
                onDieClick = onDieClick,
            )
            ResultPill(turnResult = state.turnResult)
        }
        ElyseeBanner(visible = state.elyseeBanner, onDismiss = onElyseeBannerDismiss)
    }
}

@Composable
private fun BoxScope.VotesDelta(delta: Int?) {
    // Keeps the last value so the exit animation still shows it.
    val shown = remember { mutableStateOf(delta ?: 0) }
    LaunchedEffect(delta) {
        if (delta != null) shown.value = delta
    }
    // Zero-size anchor: the "+3" hangs off the title's left edge without moving the title.
    Box(
        Modifier
            .align(Alignment.CenterStart)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(0, 0) {
                    placeable.place(
                        -placeable.width - 11.dp.roundToPx(),
                        -placeable.height / 2 - 8.dp.roundToPx(),
                    )
                }
            },
    ) {
        AnimatedVisibility(
            visible = delta != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            label = "votesDelta",
        ) {
            KoeText(
                text = "+${shown.value}",
                style = KoeTextStyles.DieNumber,
                color = colorResource(R.color.koe_text_votes),
                modifier = Modifier
                    .testTag(MatchBoardTags.VOTES_DELTA)
                    .rotate(-6f),
            )
        }
    }
}

@Composable
private fun TweetBanner(
    banner: TweetBannerUi?,
    targetIndex: Int,
    count: Int,
) {
    // Keeps the last banner so the exit animation still shows it.
    val shown = remember { mutableStateOf(banner) }
    LaunchedEffect(banner) {
        if (banner != null) shown.value = banner
    }
    // Zero-size anchor on the target chip's bottom-right corner, so the banner never moves the row.
    Box(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(Constraints())
            val gap = 12.dp.toPx()
            val chipWidth = (constraints.maxWidth - gap * (count - 1)) / count
            val chipRight = (targetIndex + 1) * chipWidth + targetIndex * gap
            val centerX = chipRight - 42.93.dp.toPx()
            val centerY = 56.dp.toPx() + 6.25.dp.toPx()
            layout(0, 0) {
                placeable.place(
                    (centerX - placeable.width / 2f).roundToInt(),
                    (centerY - placeable.height / 2f).roundToInt(),
                )
            }
        },
    ) {
        AnimatedVisibility(
            visible = banner != null,
            enter = fadeIn() + scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.5f)),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            label = "tweetBanner",
        ) {
            shown.value?.let { current ->
                TweetBannerContent(banner = current)
            }
        }
    }
}

@Composable
private fun TweetBannerContent(banner: TweetBannerUi) {
    val name = stringResource(banner.target.nameRes)
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .testTag(MatchBoardTags.TWEET_BANNER)
            .rotate(3f)
            .background(colorResource(R.color.koe_surface), shape)
            .border(BorderStroke(3.dp, colorResource(R.color.koe_action_outline)), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_board_tweet),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
        KoeText(
            text = stringResource(R.string.match_tweet_banner, name, banner.popularityLoss),
            style = KoeTextStyles.BodyStrong,
            color = colorResource(R.color.koe_text_danger),
        )
    }
}

@Composable
private fun ResultPill(turnResult: TurnResultUi?) {
    val text = turnResult?.let { turnResultText(it) }
    // Keeps the last text so the exit animation still shows it.
    val shown = remember { mutableStateOf(text.orEmpty()) }
    LaunchedEffect(text) {
        if (text != null) shown.value = text
    }
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.85f),
        label = "resultPill",
    ) {
        KoeText(
            text = text ?: shown.value,
            style = KoeTextStyles.Pill,
            color = colorResource(R.color.koe_text_on_dark),
            modifier = Modifier
                .testTag(MatchBoardTags.RESULT)
                .background(
                    colorResource(R.color.koe_action_primary_container),
                    RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun turnResultText(result: TurnResultUi): String {
    val parts = mutableListOf<String>()
    if (result.votes > 0) parts += stringResource(R.string.match_result_votes, result.votes)
    if (result.fonds > 0) parts += stringResource(R.string.match_result_fonds, result.fonds)
    if (result.popularite > 0) parts += stringResource(R.string.match_result_popularite, result.popularite)
    if (result.tweets > 0) {
        val tweets = pluralStringResource(R.plurals.match_result_tweets, result.tweets, result.tweets)
        parts += if (result.targets.isEmpty()) {
            tweets
        } else {
            val names = result.targets.map { stringResource(it.nameRes) }.joinToString(", ")
            stringResource(R.string.match_result_tweets_on, tweets, names)
        }
    }
    return if (parts.isEmpty()) {
        stringResource(R.string.match_result_nothing)
    } else {
        parts.joinToString(", ")
    }
}

@Composable
private fun Die(
    die: DieUi,
    index: Int,
    tappable: Boolean,
    kept: Boolean,
    onClick: () -> Unit,
) {
    val ring by animateColorAsState(
        targetValue = if (die.highlighted) {
            colorResource(R.color.koe_status_highlight)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "dieRing",
    )
    val description = stringResource(
        R.string.match_die_description,
        index + 1,
        when (val face = die.face) {
            is DieFace.Number -> face.value.toString()
            DieFace.Tweet -> stringResource(R.string.die_face_tweet)
            DieFace.Fonds -> stringResource(R.string.die_face_fonds)
            DieFace.Popularite -> stringResource(R.string.die_face_popularite)
        },
    )
    // No ripple: the highlight ring is the feedback.
    val interactionSource = remember { MutableInteractionSource() }
    // The tag is a sibling of the die, not a child of its background, so it is not cut by the die's edge.
    Box(Modifier.size(BoardMetrics.DieSize)) {
        Box(
            Modifier
                .testTag(MatchBoardTags.die(index))
                .semantics { contentDescription = description }
                .size(BoardMetrics.DieSize)
                .toggleable(
                    value = die.highlighted,
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = tappable,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
                .background(colorResource(R.color.koe_surface), RoundedCornerShape(16.dp))
                .drawWithContent {
                    drawContent()
                    val stroke = 3.dp.toPx()
                    drawRoundRect(
                        color = ring,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = CornerRadius(16.dp.toPx() - stroke / 2),
                        style = Stroke(stroke),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = die.face,
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut(tween(120)) + scaleOut(targetScale = 1.2f))
                },
                label = "dieFace",
            ) { face ->
                when (face) {
                    is DieFace.Number -> KoeText(
                        text = face.value.toString(),
                        style = KoeTextStyles.DieNumber,
                        color = colorResource(R.color.koe_text_primary),
                    )
                    DieFace.Tweet -> Image(
                        painter = painterResource(R.drawable.ic_board_tweet),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    DieFace.Fonds -> Image(
                        painter = painterResource(R.drawable.ic_board_fonds),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    DieFace.Popularite -> Image(
                        painter = painterResource(R.drawable.ic_board_stat_popularite),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        // Overlaps the top edge and draws above the ring; the die keeps its 56 dp box.
        if (kept) {
            KoeText(
                text = stringResource(R.string.match_die_kept),
                style = KoeTextStyles.CardBadge,
                color = colorResource(R.color.koe_text_primary),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .offset(y = (-7).dp)
                    .background(colorResource(R.color.koe_status_highlight), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun MePanel(
    me: MeUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .testTag(MatchBoardTags.ME_PANEL)
            .size(160.dp, 205.dp)
            .background(colorResource(R.color.koe_surface), RoundedCornerShape(24.dp))
            .padding(start = 14.dp, top = 13.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(candidate = me.candidate, size = 36.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.padding(top = 2.dp)) {
                KoeText(
                    text = stringResource(me.candidate.nameRes),
                    style = KoeTextStyles.Name,
                    color = colorResource(R.color.koe_text_primary),
                )
                // The "moi" label overlaps the name by 2 dp in Figma.
                KoeText(
                    text = stringResource(R.string.match_me_label),
                    style = KoeTextStyles.Caption,
                    color = colorResource(R.color.koe_text_muted),
                    modifier = Modifier.offset(y = (-2).dp),
                )
            }
        }
        Spacer(Modifier.height(17.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatRow(
                icon = R.drawable.ic_board_stat_vote,
                value = me.votes,
                valueStyle = KoeTextStyles.StatNumber,
                valueColor = colorResource(R.color.koe_text_votes),
                label = stringResource(R.string.stat_votes_max),
                labelStyle = KoeTextStyles.StatLabel,
            )
            StatRow(
                icon = R.drawable.ic_board_stat_popularite,
                value = me.popularite,
                valueStyle = KoeTextStyles.StatNumber,
                valueColor = colorResource(R.color.koe_text_popularite),
                label = stringResource(R.string.stat_popularite_max),
                labelStyle = KoeTextStyles.StatLabel,
            )
            StatRow(
                icon = R.drawable.ic_board_stat_fonds,
                value = me.fonds,
                valueStyle = KoeTextStyles.StatNumberUi,
                valueColor = colorResource(R.color.koe_text_warning),
                label = stringResource(R.string.stat_fonds),
                labelStyle = KoeTextStyles.StatLabelLarge,
            )
            StatRow(
                icon = R.drawable.ic_board_stat_mechancete,
                value = me.mechancete,
                valueStyle = KoeTextStyles.StatNumberUi,
                valueColor = colorResource(R.color.koe_text_mechancete),
                label = stringResource(R.string.stat_mechancete),
                labelStyle = KoeTextStyles.StatLabelTight,
            )
        }
    }
}

@Composable
private fun StatRow(
    icon: Int,
    value: Int,
    valueStyle: TextStyle,
    valueColor: Color,
    label: String,
    labelStyle: TextStyle,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        // Figma fixes the number column at 16 dp; wider values overflow it instead of wrapping.
        AnimatedCount(
            value = value,
            style = valueStyle,
            color = valueColor,
            modifier = Modifier.width(16.dp),
        )
        KoeText(
            text = label,
            style = labelStyle,
            color = colorResource(R.color.koe_text_muted),
        )
    }
}

@Composable
private fun SecondaryButton(
    onClick: () -> Unit,
    icon: Int?,
    label: Int,
    tag: String,
    badgeCount: Int? = null,
    pulse: Boolean = false,
    stateText: String? = null,
    badge: @Composable BoxScope.() -> Unit = {},
) {
    // The badge sits beside the surface, not inside it: the surface clips to its rounded shape.
    Box {
        PressableSurface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = colorResource(R.color.koe_action_secondary_container),
            border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
            modifier = Modifier
                .height(BoardMetrics.BottomButtonHeight)
                .testTag(tag)
                .pulsing(pulse)
                .semantics { stateText?.let { stateDescription = it } },
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                KoeText(
                    text = stringResource(label),
                    style = KoeTextStyles.CardTitle,
                    color = colorResource(R.color.koe_action_secondary_content),
                )
                if (badgeCount != null) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .background(colorResource(R.color.koe_action_primary_container), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedCount(
                            value = badgeCount,
                            style = KoeTextStyles.BadgeCount,
                            color = colorResource(R.color.koe_action_primary_content),
                        )
                    }
                }
            }
        }
        badge()
    }
}

@Composable
private fun EndTurnButton(
    visible: Boolean,
    onClick: () -> Unit,
    pulse: Boolean = false,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { it / 2 } + fadeIn(),
        exit = slideOutHorizontally { it / 2 } + fadeOut(),
        label = "endTurn",
    ) {
        PrimaryPillButton(
            label = R.string.btn_end_turn,
            tag = MatchBoardTags.END_TURN,
            onClick = onClick,
            pulse = pulse,
        )
    }
}

@Composable
private fun PrimaryPillButton(
    label: Int,
    tag: String,
    onClick: () -> Unit,
    pulse: Boolean = false,
) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        color = colorResource(R.color.koe_action_primary_container),
        modifier = Modifier
            .testTag(tag)
            .fillMaxSize()
            .pulsing(pulse),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KoeText(
                text = stringResource(label),
                style = KoeTextStyles.ButtonLarge,
                color = colorResource(R.color.koe_action_primary_content),
            )
            Image(
                painter = painterResource(R.drawable.ic_board_arrow),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** The human's step this turn; null during AI turns, which show no step. */
private enum class TurnStep { ROLL, KEEP, SHOP }

private fun turnStepOf(state: MatchUiState): TurnStep? {
    if (state.currentPlayer != state.me.candidate) return null
    val roll = state.roll
    return when {
        roll != null && roll.rollNumber == 0 -> TurnStep.ROLL
        roll != null -> TurnStep.KEEP
        state.canEndTurn -> TurnStep.SHOP
        else -> null
    }
}

/** True once the same step and roll have sat on screen long enough for the next action to pulse. */
@Composable
private fun rememberPulseReady(step: TurnStep?, rollNumber: Int?): Boolean {
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(step, rollNumber) {
        ready = false
        if (step != null) {
            delay(BoardMetrics.PulseDelayMs)
            ready = true
        }
    }
    return ready
}

/** Gentle scale on the next action; graphicsLayer leaves the layout size alone. */
@Composable
private fun Modifier.pulsing(enabled: Boolean): Modifier {
    if (!enabled) return this
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulseScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Four labels in turn order; a zero-size anchor puts the bar in the gap above the bottom buttons. */
@Composable
private fun StepBar(step: TurnStep?) {
    if (step == null) return
    val current = step.ordinal
    val currentDescription = stringResource(R.string.match_step_current)
    val labels = listOf(
        R.string.match_step_roll,
        R.string.match_step_keep,
        R.string.match_step_shop,
        R.string.match_step_end,
    )
    // The bottom buttons start 16 dp below this anchor; the bar sits above them.
    Box(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(Constraints())
            layout(0, 0) {
                placeable.place(
                    ((constraints.maxWidth - placeable.width) / 2f).roundToInt(),
                    -12.dp.roundToPx(),
                )
            }
        },
    ) {
        Row(
            Modifier.testTag(MatchBoardTags.STEP_BAR),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            labels.forEachIndexed { index, label ->
                if (index > 0) {
                    KoeText(
                        text = "›",
                        style = KoeTextStyles.Caption,
                        color = colorResource(R.color.koe_screen_board_content),
                    )
                }
                val isCurrent = index == current
                KoeText(
                    text = stringResource(label),
                    style = KoeTextStyles.Caption,
                    color = when {
                        isCurrent -> colorResource(R.color.koe_text_primary)
                        index < current -> colorResource(R.color.koe_text_muted)
                        else -> colorResource(R.color.koe_screen_board_content)
                    },
                    modifier = Modifier
                        .background(
                            if (isCurrent) colorResource(R.color.koe_status_highlight) else Color.Transparent,
                            RoundedCornerShape(10.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .semantics { if (isCurrent) stateDescription = currentDescription },
                )
            }
        }
    }
}

/** Lock or affordability marker on the corner of BOUTIQUE; it overhangs the corner, so the button keeps its size. */
@Composable
private fun BoxScope.ShopBadge(availability: ShopAvailability) {
    when (availability) {
        ShopAvailability.LOCKED -> Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-8).dp)
                .size(22.dp)
                .testTag(MatchBoardTags.SHOP_LOCK)
                .background(colorResource(R.color.koe_surface), CircleShape)
                .border(BorderStroke(2.dp, colorResource(R.color.koe_action_outline)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_board_lock),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        }
        ShopAvailability.AFFORDABLE -> Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(14.dp)
                .testTag(MatchBoardTags.SHOP_AFFORDABLE)
                .background(colorResource(R.color.koe_status_highlight), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_board_fonds),
                contentDescription = null,
                modifier = Modifier.size(10.dp),
            )
        }
        ShopAvailability.OPEN -> Unit
    }
}

/** The dice row, with the legend button on its right edge and the legend card under it. */
@Composable
private fun DiceTray(
    dice: List<DieUi>,
    tappable: Boolean,
    keeping: Boolean,
    onDieClick: (Int) -> Unit,
) {
    var legendOpen by rememberSaveable { mutableStateOf(false) }
    val legendDescription = stringResource(R.string.cd_dice_legend)
    // The legend may cover the result pill below, so the tray draws above it.
    Box(Modifier.zIndex(1f)) {
        Row(
            Modifier
                .testTag(MatchBoardTags.DICE_ROW)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dice.forEachIndexed { index, die ->
                Die(
                    die = die,
                    index = index,
                    tappable = tappable,
                    kept = keeping && die.highlighted,
                    onClick = { onDieClick(index) },
                )
            }
        }
        // Zero-size anchor on the row's bottom right: the info button tucks under the last die, clear of the MePanel.
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .layout { measurable, _ ->
                    val placeable = measurable.measure(Constraints())
                    layout(0, 0) {
                        placeable.place(-placeable.width, 4.dp.roundToPx())
                    }
                },
        ) {
            PressableSurface(
                onClick = { legendOpen = !legendOpen },
                shape = CircleShape,
                color = colorResource(R.color.koe_surface),
                border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
                modifier = Modifier
                    .size(32.dp)
                    .testTag(MatchBoardTags.DICE_LEGEND_BUTTON)
                    .semantics { contentDescription = legendDescription },
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_board_info),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        // Zero-size anchor on the row's bottom centre: the legend card hangs 8 dp below the dice.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .layout { measurable, _ ->
                    val placeable = measurable.measure(Constraints())
                    layout(0, 0) {
                        placeable.place(-placeable.width / 2, 8.dp.roundToPx())
                    }
                },
        ) {
            AnimatedVisibility(
                visible = legendOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "diceLegend",
            ) {
                DiceLegendCard(onClick = { legendOpen = false })
            }
        }
    }
}

@Composable
private fun DiceLegendCard(onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colorResource(R.color.koe_surface),
        border = BorderStroke(3.dp, colorResource(R.color.koe_action_outline)),
        modifier = Modifier
            .testTag(MatchBoardTags.DICE_LEGEND)
            .width(340.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KoeText(
                text = stringResource(R.string.match_legend_title),
                style = KoeTextStyles.CardTitle,
                color = colorResource(R.color.koe_text_primary),
            )
            KoeText(
                text = stringResource(R.string.match_legend_numbers),
                style = KoeTextStyles.Description,
                color = colorResource(R.color.koe_text_primary),
            )
            KoeText(
                text = stringResource(R.string.match_legend_tweet),
                style = KoeTextStyles.Description,
                color = colorResource(R.color.koe_text_primary),
            )
            KoeText(
                text = stringResource(R.string.match_legend_fonds),
                style = KoeTextStyles.Description,
                color = colorResource(R.color.koe_text_primary),
            )
            KoeText(
                text = stringResource(R.string.match_legend_popularite),
                style = KoeTextStyles.Description,
                color = colorResource(R.color.koe_text_primary),
            )
        }
    }
}

/** Explains the Élysée the moment the player takes it, then clears itself. */
@Composable
private fun BoxScope.ElyseeBanner(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(visible) {
        if (visible) {
            delay(BoardMetrics.ElyseeBannerMs)
            dismiss()
        }
    }
    // Zero-size anchor on the column's top centre: the banner rises over the title without moving it.
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints())
                layout(0, 0) {
                    placeable.place(-placeable.width / 2, -4.dp.roundToPx())
                }
            },
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.5f)),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            label = "elyseeBanner",
        ) {
            ElyseeBannerContent(onClick = dismiss)
        }
    }
}

@Composable
private fun ElyseeBannerContent(onClick: () -> Unit) {
    PressableSurface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colorResource(R.color.koe_surface),
        border = BorderStroke(3.dp, colorResource(R.color.koe_status_highlight)),
        modifier = Modifier
            .testTag(MatchBoardTags.ELYSEE_BANNER)
            .width(340.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_board_crown),
                contentDescription = null,
                modifier = Modifier.size(20.dp, 14.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                KoeText(
                    text = stringResource(R.string.match_elysee_banner_title),
                    style = KoeTextStyles.BodyStrong,
                    color = colorResource(R.color.koe_text_primary),
                )
                KoeText(
                    text = stringResource(R.string.match_elysee_banner_body),
                    style = KoeTextStyles.Description,
                    color = colorResource(R.color.koe_text_primary),
                )
            }
        }
    }
}

@Composable
internal fun Avatar(
    candidate: Candidate,
    size: Dp,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(colorResource(candidate.avatarColor))
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
    ) {
        val portrait = candidate.portrait
        if (portrait != null) {
            Image(
                painter = painterResource(portrait),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val silhouette = if (candidate.lightSilhouette) {
                R.drawable.ic_board_avatar_light
            } else {
                R.drawable.ic_board_avatar_dark
            }
            Image(
                painter = painterResource(silhouette),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(widthDp = 844, heightDp = 390)
@Composable
private fun MatchBoardPreview() {
    MatchBoard(
        state = mockMatchUiState(),
        onPauseClick = {},
        onOpponentClick = {},
        onShopClick = {},
        onCardsClick = {},
        onHistoryClick = {},
        onEndTurnClick = {},
        onDieClick = {},
        onRollClick = {},
        onValidateDiceClick = {},
        onElyseeBannerDismiss = {},
    )
}

private object BoardMetrics {
    val Gap = 12.dp
    val DieSize = 56.dp
    val BottomButtonHeight = 48.dp
    // Idle time before the next action pulses; the Élysée banner clears itself after its own delay.
    const val PulseDelayMs = 4_000L
    const val ElyseeBannerMs = 6_000L
}
