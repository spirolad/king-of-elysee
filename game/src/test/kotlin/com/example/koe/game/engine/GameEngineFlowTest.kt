package com.example.koe.game.engine

import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.PlayerId
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.CardEffectResult
import com.example.koe.game.shop.CardId
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.Shop
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Returns the scripted rolls in order; each roll must have exactly the requested number of dice. */
private class ScriptedDice(vararg rolls: List<DieFace>) : DiceRoller {
    private val queue = ArrayDeque(rolls.toList())

    override fun roll(count: Int): List<DieFace> {
        val next = queue.removeFirstOrNull() ?: error("No scripted roll left")
        check(next.size == count) { "Scripted roll has ${next.size} dice, engine asked for $count" }
        return next
    }

    override fun rollSingle(): DieFace = error("rollSingle is not scripted: these tests keep all dice")
}

/** Keeps every die, plays the scripted shop actions in order (Pass when exhausted) and logs its calls. */
private class RecordingController(
    private val log: MutableList<String>,
    private val shopScript: List<(List<ShopOffer>) -> ShopAction> = emptyList()
) : PlayerController {
    private var shopCalls = 0

    override suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {
        log.add("awaitFirstRoll:${player.name}")
    }

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>
    ): List<Int> = currentDice.indices.toList()

    override suspend fun shouldYieldTokyo(player: Player, attacker: Player, damage: Int): Boolean = false

    override suspend fun chooseShopAction(player: Player, offers: List<ShopOffer>, sweepCost: Int): ShopAction {
        val step = shopScript.getOrNull(shopCalls)
        shopCalls++
        return step?.invoke(offers) ?: ShopAction.Pass
    }

    override suspend fun chooseMechanceteCard(player: Player, tier: Int, choices: List<PowerCard>): PowerCard {
        log.add("mechancete:$tier:${choices.size}")
        return choices.first()
    }
}

/** Appends every observed event to the shared log. */
private class RecordingListener(private val log: MutableList<String>) : GameEventListener {
    override fun onDiceRolled(player: Player, rollNumber: Int, maxRolls: Int, dice: List<DieFace>, keptIndices: List<Int>) {
        log.add("rolled:$rollNumber/$maxRolls")
    }

    override fun onTurnSkipped(player: Player) {
        log.add("skipped:${player.name}")
    }

    override fun onMechanceteGained(player: Player, amount: Int, total: Int) {
        log.add("mechanceteGained:$amount:$total")
    }

    override fun onMechanceteCardChosen(player: Player, tier: Int, card: PowerCard, result: CardEffectResult) {
        log.add("mechanceteCard:$tier:${card.id.value}")
    }

    override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) {
        log.add("purchased:${card.id.value}:$cost")
    }

    override fun onTurnEnded(player: Player) {
        log.add("turnEnded:${player.name}")
    }
}

class GameEngineFlowTest {

    private class Game(
        val engine: GameEngine,
        val p1: Player,
        val p2: Player
    )

    private fun newGame(
        rolls: DiceRoller,
        log: MutableList<String>,
        shop: Shop = Shop(initialCards = emptyList()),
        p1Shop: List<(List<ShopOffer>) -> ShopAction> = emptyList(),
        p1Energy: Int = 0
    ): Game {
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR, initialEnergy = p1Energy)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON)
        val engine = GameEngine(
            players = listOf(p1, p2),
            controllers = mapOf(
                p1.id to RecordingController(log, p1Shop),
                p2.id to RecordingController(log)
            ),
            shop = shop,
            diceRoller = rolls,
            eventListener = RecordingListener(log)
        )
        engine.start()
        return Game(engine, p1, p2)
    }

    @Test
    fun `waits for the first roll and reports three rolls by default`() = runTest {
        val log = mutableListOf<String>()
        val game = newGame(ScriptedDice(List(6) { DieFace.ENERGY }), log)

        game.engine.playTurn()

        assertEquals(listOf("awaitFirstRoll:P1", "rolled:1/3"), log.take(2))
        assertEquals(1, log.count { it.startsWith("rolled:") })
    }

    @Test
    fun `three ones and three twos give Méchanceté and the tier-3 card at end of turn`() = runTest {
        val log = mutableListOf<String>()
        val game = newGame(ScriptedDice(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.TWO, DieFace.TWO, DieFace.TWO)), log)
        val firstTier3 = game.engine.mechanceteDeck.choicesFor(3).first()

        game.engine.playTurn()

        assertEquals(3, game.p1.mechancete)
        val expected = listOf(
            "mechanceteGained:3:3",
            "mechancete:3:4",
            "mechanceteCard:3:${firstTier3.id.value}",
            "turnEnded:P1"
        )
        assertEquals(expected, log.filter { it in expected })
        assertEquals(3, game.p1.rewardedMechanceteTier)
        assertEquals(3, game.engine.mechanceteDeck.choicesFor(3).size)
        if (firstTier3.type == CardType.KEEP) {
            assertTrue(game.p1.hasCard(firstTier3.id))
        }
    }

    @Test
    fun `a purchase is recorded with its price and round`() = runTest {
        val log = mutableListOf<String>()
        val shop = Shop(
            initialCards = listOf(
                PowerCard(CardId("cheap"), "Cheap", 2, CardType.KEEP, "test"),
                PowerCard(CardId("mid"), "Mid", 3, CardType.KEEP, "test"),
                PowerCard(CardId("pricey"), "Pricey", 4, CardType.KEEP, "test")
            ),
            marketSize = 3,
            sweepCost = 2
        )
        val buyMid: (List<ShopOffer>) -> ShopAction = { offers ->
            ShopAction.Buy(offers.first { it.cost == 3 }.card)
        }
        val game = newGame(
            rolls = ScriptedDice(List(6) { DieFace.ENERGY }),
            log = log,
            shop = shop,
            p1Shop = listOf(buyMid),
            p1Energy = 10
        )

        game.engine.playTurn()

        val purchase = game.p1.purchases.single()
        assertEquals(CardId("mid"), purchase.card.id)
        assertEquals(3, purchase.cost)
        assertEquals(1, purchase.round)
        assertTrue(log.contains("purchased:mid:3"))
    }

    @Test
    fun `a skipped turn reports it and rolls nothing`() = runTest {
        val log = mutableListOf<String>()
        val game = newGame(ScriptedDice(), log)
        game.p1.skipNextTurn = true

        game.engine.playTurn()

        assertTrue(log.contains("skipped:P1"))
        assertFalse(log.any { it.startsWith("rolled:") })
        assertEquals(game.p2.id, game.engine.currentPlayer.id)
    }
}
