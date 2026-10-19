package com.example.koe.match.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import com.example.koe.R

@Composable
fun MatchDialogHost(
    dialogs: List<MatchDialog>,
    actions: MatchDialogActions,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = dialogs.isNotEmpty()) {
        if (dialogs.last().dismissible) actions.onDismiss()
    }
    val top = dialogs.lastOrNull()
    val scrim = colorResource(R.color.koe_scrim)
    val scrimEvil = colorResource(R.color.koe_scrim_evil)
    val playerDetails = dialogs.lastOrNull { it is MatchDialog.PlayerDetails } as MatchDialog.PlayerDetails?
    val shop = dialogs.lastOrNull { it is MatchDialog.Shop } as MatchDialog.Shop?
    val myCards = dialogs.lastOrNull { it is MatchDialog.MyCards } as MatchDialog.MyCards?
    val pause = dialogs.lastOrNull { it is MatchDialog.Pause } as MatchDialog.Pause?
    val quit = dialogs.lastOrNull { it is MatchDialog.QuitConfirm } as MatchDialog.QuitConfirm?
    val yieldElysee = dialogs.lastOrNull { it is MatchDialog.YieldElysee } as MatchDialog.YieldElysee?
    val mechancete = dialogs.lastOrNull { it is MatchDialog.Mechancete } as MatchDialog.Mechancete?
    val cardDetail = dialogs.lastOrNull { it is MatchDialog.CardDetail } as MatchDialog.CardDetail?

    // Later in this order draws on top.
    Box(modifier) {
        DialogLayer(
            entry = playerDetails,
            isTop = isTop(playerDetails, top),
            sheet = true,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            PlayerDetailsDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = shop,
            isTop = isTop(shop, top),
            sheet = true,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            ShopDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = myCards,
            isTop = isTop(myCards, top),
            sheet = true,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            MyCardsDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = pause,
            isTop = isTop(pause, top),
            sheet = false,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            PauseDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = quit,
            isTop = isTop(quit, top),
            sheet = false,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            QuitConfirmDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = yieldElysee,
            isTop = isTop(yieldElysee, top),
            sheet = false,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            YieldElyseeDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = mechancete,
            isTop = isTop(mechancete, top),
            sheet = false,
            scrim = scrimEvil,
            onScrimClick = actions.onDismiss,
        ) {
            MechanceteDialog(dialog = it, actions = actions)
        }
        DialogLayer(
            entry = cardDetail,
            isTop = isTop(cardDetail, top),
            sheet = false,
            scrim = scrim,
            onScrimClick = actions.onDismiss,
        ) {
            CardDetailDialog(dialog = it, actions = actions)
        }
    }
}

private fun isTop(entry: MatchDialog?, top: MatchDialog?): Boolean = entry != null && entry === top

@Composable
private fun <T : MatchDialog> DialogLayer(
    entry: T?,
    isTop: Boolean,
    sheet: Boolean,
    scrim: Color,
    onScrimClick: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    // Plain holder, not snapshot state: the exit animation still draws the last entry it was given.
    val holder = remember { arrayOfNulls<MatchDialog>(1) }
    if (entry != null) holder[0] = entry
    @Suppress("UNCHECKED_CAST")
    val shown = holder[0] as T?
    AnimatedVisibility(
        visible = entry != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(160)),
        label = "dialogLayer",
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(scrim)
                    .testTag(MatchDialogTags.SCRIM)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isTop && shown?.dismissible == true) onScrimClick()
                    },
            )
            Box(
                Modifier
                    .align(if (sheet) Alignment.BottomCenter else Alignment.Center)
                    .animateEnterExit(
                        enter = if (sheet) slideInVertically { it } else scaleIn(initialScale = 0.92f),
                        exit = if (sheet) slideOutVertically { it } else scaleOut(targetScale = 0.92f),
                    )
                    // Eats taps so they never reach the scrim, without adding click semantics.
                    .pointerInput(Unit) { detectTapGestures() },
            ) {
                shown?.let { content(it) }
            }
        }
    }
}
