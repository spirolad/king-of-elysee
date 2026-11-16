package com.example.koe.game.engine

import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.PlayerId
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `GameEngine deterministic victory by VP`() = runTest {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON)
        val players = listOf(p1, p2)

        // Mock deterministic dice roller rolling all 3s
        val deterministicRoller = object : DiceRoller {
            override fun roll(count: Int): List<DieFace> = List(count) { DieFace.THREE }
            override fun rollSingle(): DieFace = DieFace.THREE
        }

        val controllers = mapOf(
            p1.id to AiPlayerController(),
            p2.id to AiPlayerController()
        )

        val engine = GameEngine(
            players = players,
            controllers = controllers,
            config = GameConfig(winningVictoryPoints = 10),
            diceRoller = deterministicRoller
        )

        // p1: 6 VP roll, +1 entering empty Tokyo = 7
        // p2: 6 VP roll = 6
        // p1: +2 starting turn in Tokyo = 9, then 6 VP roll = 15
        val winner = engine.runGame()
        assertEquals(p1.id, winner.id)
        assertEquals(15, winner.victoryPoints)
    }

    @Test
    fun `GameEngine deterministic victory by elimination`() = runTest {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON, initialHealth = 2)
        val players = listOf(p1, p2)

        // Mock deterministic roller rolling all Attacks
        val attackRoller = object : DiceRoller {
            override fun roll(count: Int): List<DieFace> = List(count) { DieFace.ATTACK }
            override fun rollSingle(): DieFace = DieFace.ATTACK
        }

        // Neither monster ever yields Tokyo
        val stubbornController = object : PlayerController {
            override suspend fun chooseDiceToKeep(
                player: Player,
                rollNumber: Int,
                maxRolls: Int,
                currentDice: List<DieFace>,
                currentlyKept: List<Boolean>
            ): List<Int> = currentDice.indices.toList()

            override suspend fun shouldYieldTokyo(player: Player, attacker: Player, damage: Int): Boolean = false

            override suspend fun chooseShopAction(
                player: Player,
                offers: List<ShopOffer>,
                sweepCost: Int
            ): ShopAction = ShopAction.Pass

            override suspend fun chooseMechanceteCard(player: Player, tier: Int, choices: List<PowerCard>): PowerCard =
                choices.first()
        }

        val engine = GameEngine(
            players = players,
            controllers = mapOf(p1.id to stubbornController, p2.id to stubbornController),
            diceRoller = attackRoller
        )

        // p1 enters Tokyo on turn 1; p2 hits p1 on turn 2 (p1 survives at 4);
        // p1 hits p2 for 6 on turn 3, eliminating p2 (2 health).
        val winner = engine.runGame()
        assertEquals(p1.id, winner.id)
        assertFalse(p2.isAlive)
        assertTrue(engine.isGameOver)
    }
}
