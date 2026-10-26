package com.example.koe.match.game

import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.DieFace as RolledFace
import com.example.koe.game.shop.Shop
import com.example.koe.match.Candidate
import com.example.koe.match.DieFace
import com.example.koe.match.RollUi
import com.example.koe.match.dialog.GameOverReason
import com.example.koe.match.dialog.MatchDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private fun viewModel(vararg turns: List<RolledFace>, aiDelayMillis: Long = 0) = MatchViewModel(
        Candidate.MACRON,
        diceRoller = QueueDiceRoller(*turns),
        shop = Shop(initialCards = emptyList()),
        aiDelayMillis = aiDelayMillis,
    )

    private fun energyRoll() = List(6) { RolledFace.ENERGY }

    private fun highlighted(viewModel: MatchViewModel) = viewModel.uiState.value.dice.map { it.highlighted }

    /** The human throws once and keeps every die, which ends the first roll. */
    private fun TestScope.throwAndKeepAll(viewModel: MatchViewModel) {
        viewModel.onRollClick()
        advanceUntilIdle()
        viewModel.onValidateDiceClick()
        advanceUntilIdle()
    }

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
    fun rollPhaseBeforeTheFirstThrow() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RollUi(0, 3), state.roll)
        assertTrue(state.dice.isEmpty())
        assertFalse(state.canEndTurn)
    }

    @Test
    fun keepingDiceAndRerolling() = runTest {
        val viewModel = viewModel(
            listOf(
                RolledFace.ONE,
                RolledFace.ONE,
                RolledFace.ONE,
                RolledFace.ENERGY,
                RolledFace.ENERGY,
                RolledFace.ENERGY,
            ),
        )
        viewModel.start()
        advanceUntilIdle()

        viewModel.onRollClick()
        advanceUntilIdle()
        assertEquals(RollUi(1, 3), viewModel.uiState.value.roll)

        viewModel.onDieClick(0)
        viewModel.onDieClick(1)
        viewModel.onDieClick(2)
        assertEquals(listOf(true, true, true, false, false, false), highlighted(viewModel))

        viewModel.onDieClick(2)
        assertFalse(highlighted(viewModel)[2])
        viewModel.onDieClick(2)
        assertTrue(highlighted(viewModel)[2])

        viewModel.onRollClick()
        advanceUntilIdle()
        val rerolled = viewModel.uiState.value
        assertEquals(RollUi(2, 3), rerolled.roll)
        assertEquals(List(3) { DieFace.Fonds }, rerolled.dice.drop(3).map { it.face })

        viewModel.onValidateDiceClick()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertNull(state.roll)
        assertTrue(state.canEndTurn)
        val result = checkNotNull(state.turnResult)
        assertEquals(3, result.fonds)
        assertEquals(1, result.votes)
    }

    @Test
    fun dieTapsAreIgnoredOutsideTheKeepPhase() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()

        viewModel.onDieClick(0)

        val state = viewModel.uiState.value
        assertEquals(RollUi(0, 3), state.roll)
        assertTrue(state.dice.isEmpty())
    }

    @Test
    fun firstRollWaitsInTheShopForTheHuman() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

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
        throwAndKeepAll(viewModel)

        viewModel.onShopClick()
        val shop = viewModel.dialogs.value.last() as MatchDialog.Shop
        assertEquals(6, shop.fonds)

        viewModel.onDismiss()
        assertTrue(viewModel.dialogs.value.isEmpty())
    }

    @Test
    fun endingTurnPlaysTheAisAndReturnsToTheHumansNextThrow() = runTest {
        val viewModel = viewModel(energyRoll())
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        viewModel.onEndTurnClick()
        advanceUntilIdle()

        assertEquals(RollUi(0, 3), viewModel.uiState.value.roll)
        assertFalse(viewModel.uiState.value.canEndTurn)
        viewModel.onPauseClick()
        assertEquals(2, (viewModel.dialogs.value.last() as MatchDialog.Pause).turn)
    }

    @Test
    fun aiAttackOpensTheYieldDialogUntilTheHumanStays() = runTest {
        val viewModel = viewModel(energyRoll(), List(6) { RolledFace.ATTACK })
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        viewModel.onEndTurnClick()
        advanceUntilIdle()

        val yieldDialog = viewModel.dialogs.value.last() as MatchDialog.YieldElysee
        assertEquals(Candidate.MELENCHON, yieldDialog.attacker)
        assertEquals(6, yieldDialog.popularityLoss)

        viewModel.onStay()
        assertEquals(4, viewModel.uiState.value.me.popularite)
        assertTrue(viewModel.dialogs.value.none { it is MatchDialog.YieldElysee })
    }

    @Test
    fun aiTurnShowsWhoIsPlaying() = runTest {
        val viewModel = MatchViewModel(
            Candidate.MACRON,
            diceRoller = QueueDiceRoller(energyRoll()),
            shop = Shop(initialCards = emptyList()),
            aiDelayMillis = 1_000,
        )
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        viewModel.onEndTurnClick()
        runCurrent()

        val aiTurn = viewModel.uiState.value
        assertEquals(Candidate.MELENCHON, aiTurn.currentPlayer)
        assertEquals(
            mapOf(
                Candidate.MELENCHON to true,
                Candidate.LEPEN to false,
                Candidate.POUTOU to false,
            ),
            aiTurn.opponents.associate { it.candidate to it.isPlaying },
        )
        assertFalse(aiTurn.canEndTurn)
        assertNull(aiTurn.roll)

        advanceUntilIdle()

        val humanTurn = viewModel.uiState.value
        assertEquals(Candidate.MACRON, humanTurn.currentPlayer)
        assertFalse(humanTurn.opponents.any { it.isPlaying })
    }

    @Test
    fun aiFinalKeepIsHighlighted() = runTest {
        val viewModel = MatchViewModel(
            Candidate.MACRON,
            diceRoller = QueueDiceRoller(energyRoll(), energyRoll()),
            shop = Shop(initialCards = emptyList()),
            aiDelayMillis = 1_000,
        )
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        viewModel.onEndTurnClick()
        // The AI throws after 1 000 ms and keeps all after another 1 000 ms; its shop waits 2 000 ms more.
        advanceTimeBy(2_500)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(6, state.dice.size)
        assertEquals(List(6) { true }, highlighted(viewModel))
    }

    @Test
    fun humanWinsOnVotes() = runTest {
        // The human's three 3s give 6 votes a turn; the AIs only collect energy.
        val humanThrow = List(6) { RolledFace.THREE }
        val turns = List(3) { listOf(humanThrow, energyRoll(), energyRoll(), energyRoll()) }.flatten()
        val viewModel = viewModel(*turns.toTypedArray())
        viewModel.start()
        advanceUntilIdle()

        // Turn 1: 6 votes and 1 for entering the Elysee. Turn 2: 2 for staying and 6, so 15.
        // Turn 3: 2 and 6 more reach 23, past the 20 needed.
        throwAndKeepAll(viewModel)
        viewModel.onEndTurnClick()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)
        viewModel.onEndTurnClick()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        val dialogs = viewModel.dialogs.value
        assertEquals(1, dialogs.size)
        val gameOver = dialogs.single() as MatchDialog.GameOver
        assertTrue(gameOver.youWon)
        assertEquals(Candidate.MACRON, gameOver.winner)
        assertEquals(GameOverReason.VOTES, gameOver.reason)
        assertFalse(viewModel.uiState.value.canEndTurn)
    }

    @Test
    fun humanEliminatedEndsTheMatch() = runTest {
        // The AI pause is 1 ms, not 0: a cancelled match stops only at a suspension, and a 0 ms pause never suspends.
        val viewModel = viewModel(
            energyRoll(),
            List(6) { RolledFace.ATTACK },
            List(6) { RolledFace.ATTACK },
            aiDelayMillis = 1,
        )
        viewModel.start()
        advanceUntilIdle()
        throwAndKeepAll(viewModel)

        viewModel.onEndTurnClick()
        advanceUntilIdle()
        // Melenchon's attack leaves the human at 4 popularite; Le Pen's attack then takes the rest.
        viewModel.onStay()
        advanceUntilIdle()

        val dialogs = viewModel.dialogs.value
        assertEquals(1, dialogs.size)
        val gameOver = dialogs.single() as MatchDialog.GameOver
        assertEquals(GameOverReason.ELIMINATED, gameOver.reason)
        assertNull(gameOver.winner)
        assertFalse(gameOver.youWon)
        assertNull(viewModel.uiState.value.roll)

        advanceUntilIdle()
        assertEquals(dialogs, viewModel.dialogs.value)
    }
}
