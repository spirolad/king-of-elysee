package com.example.koe.match

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
    onEndTurnClick: () -> Unit,
    dialogActions: MatchDialogActions,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = MatchBoardDefaultPadding,
) {
    // The dialogs sit on the full frame, so they are not padded by the board's cutout insets.
    Box(modifier.fillMaxSize()) {
        MatchBoard(
            state = state,
            onPauseClick = onPauseClick,
            onOpponentClick = onOpponentClick,
            onShopClick = onShopClick,
            onCardsClick = onCardsClick,
            onEndTurnClick = onEndTurnClick,
            contentPadding = contentPadding,
        )
        MatchDialogHost(
            dialogs = dialogs,
            actions = dialogActions,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
