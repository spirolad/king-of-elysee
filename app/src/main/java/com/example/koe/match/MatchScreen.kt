package com.example.koe.match

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.MatchDialogActions
import com.example.koe.match.dialog.MatchDialogHost

@Composable
fun MatchScreen(
    state: MatchUiState,
    dialogs: List<MatchDialog>,
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
    dialogActions: MatchDialogActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MatchBoardDefaultPadding,
) {
    // Back on the bare board pauses instead of leaving the match; QUITTER in the pause dialog is the way out.
    BackHandler(enabled = dialogs.isEmpty(), onBack = onPauseClick)
    // The dialogs sit on the full frame, so they are not padded by the board's cutout insets.
    Box(modifier.fillMaxSize()) {
        MatchBoard(
            state = state,
            onPauseClick = onPauseClick,
            onOpponentClick = onOpponentClick,
            onShopClick = onShopClick,
            onCardsClick = onCardsClick,
            onHistoryClick = onHistoryClick,
            onEndTurnClick = onEndTurnClick,
            onDieClick = onDieClick,
            onRollClick = onRollClick,
            onValidateDiceClick = onValidateDiceClick,
            onElyseeBannerDismiss = onElyseeBannerDismiss,
            contentPadding = contentPadding,
        )
        MatchDialogHost(
            dialogs = dialogs,
            actions = dialogActions,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
