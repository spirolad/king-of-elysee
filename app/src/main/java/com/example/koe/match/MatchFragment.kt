package com.example.koe.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koe.R
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.MatchDialogActions
import com.example.koe.match.dialog.mockMyCardsDialog
import com.example.koe.match.dialog.mockPauseDialog
import com.example.koe.match.dialog.mockPlayerDetailsDialog
import com.example.koe.match.dialog.mockShopDialog
import com.example.koe.match.dialog.mockYieldDialog

class MatchFragment : Fragment() {

    private var state by mutableStateOf(mockMatchUiState())

    // Top of the list is the visible dialog; the host draws the rest underneath.
    private var dialogs by mutableStateOf(emptyList<MatchDialog>())

    private fun push(dialog: MatchDialog) {
        dialogs = dialogs + dialog
    }

    private fun pop() {
        dialogs = dialogs.dropLast(1)
    }

    private fun replaceTop(dialog: MatchDialog) {
        dialogs = dialogs.dropLast(1) + dialog
    }


    private val dialogActions = MatchDialogActions(
        onDismiss = { pop() },
        onResume = { pop() },
        // No rules or settings screens yet.
        onRules = {},
        onSettings = {},
        // The pause dialog is replaced, not stacked, so the confirmation is the only one left.
        onQuitRequest = { replaceTop(MatchDialog.QuitConfirm(state.king)) },
        onQuitConfirmed = {
            dialogs = emptyList()
            findNavController().popBackStack(R.id.homeFragment, false)
        },
        onStay = { pop() },
        onYield = { pop() },
        onMechanceteValidate = { pop() },
        onBuy = { pop() },
        onRenew = {},
        onCardDetails = { push(MatchDialog.CardDetail(it)) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            // The board is drawn edge to edge; Figma's 47 dp edge already leaves room for the camera
            // cutout, so it only grows when a cutout is deeper than that.
            val density = LocalDensity.current
            val direction = LocalLayoutDirection.current
            val cutout = WindowInsets.displayCutout
            val edge = dimensionResource(R.dimen.koe_board_edge)
            val start = with(density) { maxOf(edge, cutout.getLeft(density, direction).toDp()) }
            val end = with(density) { maxOf(edge, cutout.getRight(density, direction).toDp()) }

            MatchScreen(
                state = state,
                dialogs = dialogs,
                onPauseClick = { push(mockPauseDialog()) },
                onOpponentClick = { candidate ->
                    val opponent = state.opponents.first { it.candidate == candidate }
                    push(
                        mockPlayerDetailsDialog(
                            candidate = candidate,
                            isKing = opponent.isKing,
                            popularite = opponent.popularite,
                            votes = opponent.votes,
                        ),
                    )
                },
                onShopClick = { push(mockShopDialog()) },
                onCardsClick = { push(mockMyCardsDialog()) },
                onEndTurnClick = { push(mockYieldDialog()) },
                dialogActions = dialogActions,
                contentPadding = PaddingValues(start = start, top = 12.dp, end = end, bottom = 21.dp),
            )
        }
    }
}
