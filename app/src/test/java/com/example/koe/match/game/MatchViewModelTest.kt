package com.example.koe.match.game

import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.DieFace as RolledFace
import com.example.koe.match.Candidate
import com.example.koe.match.DieFace
import com.example.koe.match.dialog.MatchDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Serves the queued rolls, one per turn; once empty every die shows energy. */
class QueueDiceRoller(vararg turns: List<RolledFace>) : DiceRoller {
    private val queue = ArrayDeque(turns.toList())

    override fun roll(count: Int): List<RolledFace> {
        val next = queue.removeFirstOrNull() ?: return List(count) { RolledFace.ENERGY }
        return List(count) { next.getOrElse(it) { RolledFace.ENERGY } }
    }

    override fun rollSingle(): RolledFace = RolledFace.ENERGY
}

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(vararg turns: List<RolledFace>) = MatchViewModel(
        Candidate.MACRON,
        diceRoller = QueueDiceRoller(*turns),
        aiDelayMillis = 0,
    )

    private fun energyRoll() = List(6) { RolledFace.ENERGY }

    @Test
    fun initialStateBeforeStartHasNoDialogs() {
        val viewModel = viewModel()
        val state = viewModel.uiState.value

        assertEquals(Candidate.MACRON, state.me.candidate)
        assertEquals(
            listOf(Candidate.MELENCHON, Candidate.LEPEN, Candidate.POUTOU),
            state.opponents.map { it.candidate },
        )
        assertNull(state.king)
        assertEquals(0, state.me.votes)
        assertEquals(10, state.me.popularite)
        assertTrue(viewModel.dialogs.value.isEmpty())
    }

    @Test
    fun firstRollWaitsInTheShopForTheHuman() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.canEndTurn)
        assertEquals(6, state.me.fonds)
        assertEquals(List(6) { DieFace.Fonds }, state.dice.map { it.face })
        assertEquals(Candidate.MACRON, state.king)
    }

    @Test
    fun shopOpensWithTheHumanFundsAndDismisses() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()

        viewModel.onShopClick()
        val shop = viewModel.dialogs.value.last() as MatchDialog.Shop
        assertEquals(6, shop.fonds)

        viewModel.onDismiss()
        assertTrue(viewModel.dialogs.value.isEmpty())
    }

    @Test
    fun endingTurnPlaysTheAisAndReturnsToTheHumanShop() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()

        viewModel.onEndTurnClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canEndTurn)
        viewModel.onPauseClick()
        assertEquals(2, (viewModel.dialogs.value.last() as MatchDialog.Pause).turn)
    }

    @Test
    fun aiAttackOpensTheYieldDialogUntilTheHumanStays() = runTest {
        val viewModel = viewModel(energyRoll(), List(6) { RolledFace.ATTACK })
        viewModel.start()
        advanceUntilIdle()

        viewModel.onEndTurnClick()
        advanceUntilIdle()

        val yieldDialog = viewModel.dialogs.value.last() as MatchDialog.YieldElysee
        assertEquals(Candidate.MELENCHON, yieldDialog.attacker)
        assertEquals(6, yieldDialog.popularityLoss)

        viewModel.onStay()
        assertEquals(4, viewModel.uiState.value.me.popularite)
        assertTrue(viewModel.dialogs.value.none { it is MatchDialog.YieldElysee })
    }
}
