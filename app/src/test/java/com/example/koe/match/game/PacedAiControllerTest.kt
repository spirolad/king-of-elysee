package com.example.koe.match.game

import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import com.example.koe.match.Candidate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Answers every decision with a fixed choice and records the calls it received. */
private class FakeController : PlayerController {
    val calls = mutableListOf<String>()

    override suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {
        calls += "awaitFirstRoll"
    }

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>,
    ): List<Int> {
        calls += "chooseDiceToKeep"
        return currentDice.indices.toList()
    }

    override suspend fun shouldYieldTokyo(player: Player, attacker: Player, damage: Int): Boolean {
        calls += "shouldYieldTokyo"
        return false
    }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int,
    ): ShopAction {
        calls += "chooseShopAction"
        return ShopAction.Pass
    }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>,
    ): PowerCard {
        calls += "chooseMechanceteCard"
        return choices.first()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PacedAiControllerTest {

    private val player = Candidate.MELENCHON.toPlayer()

    @Test
    fun `first shop decision of a turn waits the result time and the next ones the think time`() = runTest {
        val controller = PacedAiController(FakeController(), thinkMillis = 100, resultMillis = 500)
        controller.awaitFirstRoll(player, maxRolls = 3)

        val firstStart = currentTime
        controller.chooseShopAction(player, offers = emptyList(), sweepCost = 2)
        assertEquals(500, currentTime - firstStart)

        val secondStart = currentTime
        controller.chooseShopAction(player, offers = emptyList(), sweepCost = 2)
        assertEquals(100, currentTime - secondStart)
    }

    @Test
    fun `dice decisions wait the think time`() = runTest {
        val controller = PacedAiController(FakeController(), thinkMillis = 100, resultMillis = 500)
        controller.awaitFirstRoll(player, maxRolls = 3)

        val start = currentTime
        controller.chooseDiceToKeep(
            player,
            rollNumber = 1,
            maxRolls = 3,
            currentDice = List(6) { DieFace.ENERGY },
            currentlyKept = List(6) { false },
        )
        assertEquals(100, currentTime - start)
    }

    @Test
    fun `a new turn makes the next shop decision wait the result time again`() = runTest {
        val controller = PacedAiController(FakeController(), thinkMillis = 100, resultMillis = 500)
        controller.awaitFirstRoll(player, maxRolls = 3)
        controller.chooseShopAction(player, offers = emptyList(), sweepCost = 2)

        controller.awaitFirstRoll(player, maxRolls = 3)
        val start = currentTime
        controller.chooseShopAction(player, offers = emptyList(), sweepCost = 2)
        assertEquals(500, currentTime - start)
    }

    @Test
    fun `decisions are forwarded to the delegate`() = runTest {
        val delegate = FakeController()
        val controller = PacedAiController(delegate, thinkMillis = 100, resultMillis = 500)
        controller.awaitFirstRoll(player, maxRolls = 3)
        controller.chooseShopAction(player, offers = emptyList(), sweepCost = 2)

        assertEquals(listOf("awaitFirstRoll", "chooseShopAction"), delegate.calls)
    }
}
