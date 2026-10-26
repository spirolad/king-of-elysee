package com.example.koe.match.game

import com.example.koe.game.dice.DieFace as EngineDieFace
import com.example.koe.game.engine.GameEngine
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.CardPurchase
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.ShopOffer
import com.example.koe.match.Candidate
import com.example.koe.match.DieFace
import com.example.koe.match.dialog.CardKind
import com.example.koe.match.dialog.CardPaymentUi
import com.example.koe.match.dialog.MatchDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchGameMapperTest {

    private fun newEngine(): GameEngine {
        val players = Candidate.entries.map { it.toPlayer() }
        return GameEngine(
            players = players,
            controllers = players.associate { it.id to AiPlayerController() },
        )
    }

    private fun GameEngine.player(candidate: Candidate): Player =
        players.first { it.candidate == candidate }

    private fun affichage() = CardCatalog.createPowerCards().first { it.id.value == "affichage" }

    @Test
    fun `engine die faces map to ui faces`() {
        assertEquals(DieFace.Number(1), EngineDieFace.ONE.toUi())
        assertEquals(DieFace.Number(2), EngineDieFace.TWO.toUi())
        assertEquals(DieFace.Number(3), EngineDieFace.THREE.toUi())
        assertEquals(DieFace.Fonds, EngineDieFace.ENERGY.toUi())
        assertEquals(DieFace.Tweet, EngineDieFace.ATTACK.toUi())
        assertEquals(DieFace.Popularite, EngineDieFace.HEAL.toUi())
    }

    @Test
    fun `candidate round trips through the engine player`() {
        Candidate.entries.forEach { candidate ->
            val player = candidate.toPlayer()
            assertEquals(PlayerId(candidate.name), player.id)
            assertEquals(candidate.name, player.name)
            assertEquals(candidate, player.candidate)
        }
    }

    @Test
    fun `next mechanceté tier follows the tiers and stops at the last one`() {
        assertEquals(3, nextMechanceteTier(0))
        assertEquals(6, nextMechanceteTier(3))
        assertEquals(10, nextMechanceteTier(7))
        assertEquals(10, nextMechanceteTier(12))
    }

    @Test
    fun `keep card maps to pouvoir with its printed cost`() {
        val card = affichage()
        val ui = card.toUi()
        assertEquals("affichage", ui.id)
        assertEquals(CardKind.POUVOIR, ui.kind)
        assertEquals(card.cost, ui.cost)
    }

    @Test
    fun `discard card maps to action`() {
        val card = CardCatalog.createActionCards().first { it.type == CardType.DISCARD }
        val ui = card.toUi()
        assertEquals(CardKind.ACTION, ui.kind)
        assertEquals(card.cost, ui.cost)
    }

    @Test
    fun `wickedness card maps to mechancete without cost`() {
        val tile = CardCatalog.createWickednessTiles().first()
        val ui = tile.toUi()
        assertEquals(CardKind.MECHANCETE, ui.kind)
        assertNull(ui.cost)
    }

    @Test
    fun `market card is not owned and has no payment`() {
        val ui = affichage().toUi(cost = 1, enabled = false)
        assertEquals(1, ui.cost)
        assertFalse(ui.owned)
        assertFalse(ui.enabled)
        assertNull(ui.paid)
    }

    @Test
    fun `owned card carries the recorded purchase`() {
        val player = Candidate.MACRON.toPlayer()
        val card = affichage()
        player.addCard(card)
        player.recordPurchase(CardPurchase(card, cost = 2, round = 4))

        val ui = card.toUi(owner = player)
        assertTrue(ui.owned)
        assertEquals(CardPaymentUi(cost = 2, turn = 4), ui.paid)
    }

    @Test
    fun `board state lists me first then opponents and tracks the king`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)
        lepen.damage(3)
        lepen.addVictoryPoints(5)
        lepen.addEnergy(4)
        lepen.addMechancete(2)
        lepen.addCard(affichage())
        engine.player(Candidate.MACRON).addVictoryPoints(2)
        val dice = listOf(EngineDieFace.ATTACK, EngineDieFace.ONE, EngineDieFace.HEAL)

        val before = boardState(engine, lepen.id, dice, listOf(0, 2), canEndTurn = true)
        assertNull(before.king)
        assertEquals(
            listOf(Candidate.MACRON, Candidate.MELENCHON, Candidate.POUTOU),
            before.opponents.map { it.candidate },
        )
        assertEquals(Candidate.MACRON, before.opponents[0].candidate)
        assertEquals(10, before.opponents[0].popularite)
        assertEquals(2, before.opponents[0].votes)
        assertEquals(Candidate.LEPEN, before.me.candidate)
        assertEquals(5, before.me.votes)
        assertEquals(7, before.me.popularite)
        assertEquals(4, before.me.fonds)
        assertEquals(2, before.me.mechancete)
        assertEquals(1, before.cardsCount)
        assertTrue(before.canEndTurn)
        assertNull(before.turnResult)
        assertEquals(
            listOf(DieFace.Tweet, DieFace.Number(1), DieFace.Popularite),
            before.dice.map { it.face },
        )
        assertEquals(listOf(true, false, true), before.dice.map { it.highlighted })

        engine.tokyoBoard.enter(engine.player(Candidate.MELENCHON))
        val after = boardState(engine, lepen.id, dice, emptyList(), canEndTurn = false)
        assertEquals(Candidate.MELENCHON, after.king)
        assertTrue(after.opponents.first { it.candidate == Candidate.MELENCHON }.isKing)
        assertFalse(after.opponents.first { it.candidate == Candidate.MACRON }.isKing)
        assertFalse(after.canEndTurn)
    }

    @Test
    fun `pause ranking sorts by votes with ties in engine order`() {
        val engine = newEngine()
        engine.player(Candidate.POUTOU).addVictoryPoints(7)
        engine.player(Candidate.MACRON).addVictoryPoints(7)
        engine.player(Candidate.LEPEN).addVictoryPoints(3)

        val dialog = pauseDialog(engine)
        assertEquals(1, dialog.turn)
        assertEquals(Candidate.MACRON, dialog.currentPlayer)
        assertEquals(
            listOf(Candidate.MACRON, Candidate.POUTOU, Candidate.LEPEN, Candidate.MELENCHON),
            dialog.ranking.map { it.candidate },
        )
        assertEquals(listOf(7, 7, 3, 0), dialog.ranking.map { it.votes })
    }

    @Test
    fun `shop enables only affordable cards when buying is allowed`() {
        val engine = newEngine()
        val me = engine.player(Candidate.MACRON)
        me.addEnergy(4)
        val cards = CardCatalog.createPowerCards().take(2)
        val offers = listOf(ShopOffer(cards[0], 3), ShopOffer(cards[1], 5))

        val open = shopDialog(me, offers, sweepCost = 2, canBuy = true)
        assertEquals(4, open.fonds)
        assertEquals(2, open.renewCost)
        assertEquals(listOf(3, 5), open.cards.map { it.cost })
        assertEquals(listOf(true, false), open.cards.map { it.enabled })

        val closed = shopDialog(me, offers, sweepCost = 2, canBuy = false)
        assertEquals(listOf(false, false), closed.cards.map { it.enabled })
    }

    @Test
    fun `yield dialog reports the king, attacker and popularity`() {
        val engine = newEngine()
        val king = engine.player(Candidate.LEPEN)
        king.damage(3)
        val attacker = engine.player(Candidate.MACRON)

        val dialog = yieldDialog(king, attacker, damage = 2)
        assertEquals(
            MatchDialog.YieldElysee(
                king = Candidate.LEPEN,
                attacker = Candidate.MACRON,
                popularityLoss = 2,
                popularityLeft = 7,
            ),
            dialog,
        )
    }
}
