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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun `GameEngine deterministic victory by VP`() {
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

        val winner = engine.runGame()
        assertEquals(p1.id, winner.id)
        assertTrue(winner.victoryPoints >= 10)
    }

    @Test
    fun `GameEngine deterministic victory by elimination`() {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON, initialHealth = 2)
        val players = listOf(p1, p2)

        // Mock deterministic roller rolling all Attacks
        val attackRoller = object : DiceRoller {
            override fun roll(count: Int): List<DieFace> = List(count) { DieFace.ATTACK }
            override fun rollSingle(): DieFace = DieFace.ATTACK
        }

        // p2 never yields Tokyo
        val stubbornController = object : PlayerController {
            override fun chooseDiceToKeep(player: Player, rollNumber: Int, currentDice: List<DieFace>, currentlyKept: List<Boolean>): List<Int> = currentDice.indices.toList()
            override fun shouldYieldTokyo(player: Player, attacker: Player, damage: Int): Boolean = false
            override fun chooseShopAction(player: Player, availableCards: List<PowerCard>, sweepCost: Int): ShopAction = ShopAction.Pass
        }

        val engine = GameEngine(
            players = players,
            controllers = mapOf(p1.id to stubbornController, p2.id to stubbornController),
            diceRoller = attackRoller
        )

        // First turn: p1 enters Tokyo (as Tokyo is empty)
        // Second turn: p2 rolls attacks from outside and damages p1 in Tokyo
        // Next turns continue until elimination
        val winner = engine.runGame()
        assertTrue(winner.isAlive)
        assertTrue(engine.isGameOver)
    }
}
