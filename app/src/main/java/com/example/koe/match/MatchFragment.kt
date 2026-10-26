package com.example.koe.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.example.koe.R
import com.example.koe.match.dialog.MatchDialogActions
import com.example.koe.match.game.MatchViewModel

class MatchFragment : Fragment() {

    private val viewModel: MatchViewModel by viewModels { MatchViewModel.factory(candidateArg()) }

    // Lambdas keep the ViewModel untouched until the view exists.
    private val dialogActions = MatchDialogActions(
        onDismiss = { viewModel.onDismiss() },
        onResume = { viewModel.onResume() },
        // No rules or settings screens yet.
        onRules = {},
        onSettings = {},
        onQuitRequest = { viewModel.onQuitRequest() },
        onQuitConfirmed = {
            viewModel.onQuitConfirmed()
            findNavController().popBackStack(R.id.homeFragment, false)
        },
        onStay = { viewModel.onStay() },
        onYield = { viewModel.onYield() },
        onMechanceteValidate = { viewModel.onMechanceteValidate(it) },
        onBuy = { viewModel.onBuy(it) },
        onRenew = { viewModel.onRenew() },
        onCardDetails = { viewModel.onCardDetails(it) },
    )

    private fun candidateArg(): Candidate =
        requireArguments().getString("candidate")?.let { Candidate.valueOf(it) } ?: Candidate.MACRON

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val dialogs by viewModel.dialogs.collectAsStateWithLifecycle()

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
                onPauseClick = viewModel::onPauseClick,
                onOpponentClick = viewModel::onOpponentClick,
                onShopClick = viewModel::onShopClick,
                onCardsClick = viewModel::onCardsClick,
                onEndTurnClick = viewModel::onEndTurnClick,
                dialogActions = dialogActions,
                contentPadding = PaddingValues(start = start, top = 12.dp, end = end, bottom = 21.dp),
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
    }
}
