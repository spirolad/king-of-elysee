package com.example.koe.match.game

import com.example.koe.game.dice.DieFace as EngineDieFace
import com.example.koe.game.engine.GameEngine
import com.example.koe.game.engine.WinReason
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.CardPurchase
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.ShopOffer
import com.example.koe.match.Candidate
import com.example.koe.match.DieFace
import com.example.koe.match.RollUi
import com.example.koe.match.ShopAvailability
import com.example.koe.match.TurnResultUi
import com.example.koe.match.TweetBannerUi
import com.example.koe.match.dialog.CardKind
import com.example.koe.match.dialog.CardPaymentUi
import com.example.koe.match.dialog.GameOverReason
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
    fun `fresh board state has the first player as current and no opponent playing`() {
        val engine = newEngine()
        val state = boardState(engine, Candidate.MACRON.toPlayer().id, emptyList(), emptyList(), canEndTurn = false)

        assertEquals(Candidate.MACRON, state.currentPlayer)
        assertFalse(state.opponents.any { it.isPlaying })
    }

    @Test
    fun `board state marks only the opponent whose turn it is as playing`() {
        val players = listOf(Candidate.MELENCHON, Candidate.LEPEN, Candidate.MACRON, Candidate.POUTOU)
            .map { it.toPlayer() }
        val engine = GameEngine(
            players = players,
            controllers = players.associate { it.id to AiPlayerController() },
        )
        val lepen = engine.player(Candidate.LEPEN)

        val state = boardState(engine, lepen.id, emptyList(), emptyList(), canEndTurn = false)

        assertEquals(Candidate.MELENCHON, state.currentPlayer)
        assertEquals(
            listOf(Candidate.MELENCHON),
            state.opponents.filter { it.isPlaying }.map { it.candidate },
        )
        assertEquals(
            listOf(Candidate.MELENCHON, Candidate.MACRON, Candidate.POUTOU),
            state.opponents.map { it.candidate },
        )
        assertEquals(listOf(true, false, false), state.opponents.map { it.isPlaying })
    }

    @Test
    fun `eliminated opponent is not shown as playing even on its turn`() {
        val players = listOf(Candidate.MELENCHON, Candidate.LEPEN, Candidate.MACRON, Candidate.POUTOU)
            .map { it.toPlayer() }
        val engine = GameEngine(
            players = players,
            controllers = players.associate { it.id to AiPlayerController() },
        )
        val lepen = engine.player(Candidate.LEPEN)
        engine.player(Candidate.MELENCHON).damage(100)

        val state = boardState(engine, lepen.id, emptyList(), emptyList(), canEndTurn = false)

        assertEquals(Candidate.MELENCHON, state.currentPlayer)
        val melenchon = state.opponents.first { it.candidate == Candidate.MELENCHON }
        assertTrue(melenchon.isEliminated)
        assertFalse(melenchon.isPlaying)
        assertFalse(state.opponents.first { it.candidate == Candidate.MACRON }.isEliminated)
    }

    @Test
    fun `board state passes the roll, turn result, votes delta and tweet banner through`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)
        val roll = RollUi(rollNumber = 1, maxRolls = 3)
        val turnResult = TurnResultUi(
            votes = 2,
            fonds = 1,
            popularite = 0,
            tweets = 2,
            targets = listOf(Candidate.MACRON),
        )
        val banner = TweetBannerUi(target = Candidate.MACRON, popularityLoss = 2)

        val state = boardState(
            engine,
            lepen.id,
            listOf(EngineDieFace.ATTACK),
            keptIndices = emptyList(),
            canEndTurn = false,
            roll = roll,
            turnResult = turnResult,
            votesDelta = 2,
            tweetBanner = banner,
        )
        assertEquals(roll, state.roll)
        assertEquals(turnResult, state.turnResult)
        assertEquals(2, state.votesDelta)
        assertEquals(banner, state.tweetBanner)
    }

    @Test
    fun `board state passes the shop availability and the elysee banner through`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)

        val state = boardState(
            engine,
            lepen.id,
            emptyList(),
            emptyList(),
            canEndTurn = true,
            shopAvailability = ShopAvailability.AFFORDABLE,
            elyseeBanner = true,
        )
        assertEquals(ShopAvailability.AFFORDABLE, state.shopAvailability)
        assertTrue(state.elyseeBanner)

        val defaults = boardState(engine, lepen.id, emptyList(), emptyList(), canEndTurn = false)
        assertEquals(ShopAvailability.LOCKED, defaults.shopAvailability)
        assertFalse(defaults.elyseeBanner)
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
    fun `game over by votes names the winner and whether it was me`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)
        lepen.addVictoryPoints(20)

        val dialog = gameOverDialog(engine, lepen.id, lepen, WinReason.VictoryPointsReached(20))
        assertEquals(Candidate.LEPEN, dialog.winner)
        assertTrue(dialog.youWon)
        assertEquals(GameOverReason.VOTES, dialog.reason)
    }

    @Test
    fun `last monster standing won by an opponent is not my win`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)
        val macron = engine.player(Candidate.MACRON)

        val dialog = gameOverDialog(engine, lepen.id, macron, WinReason.LastMonsterStanding)
        assertEquals(Candidate.MACRON, dialog.winner)
        assertFalse(dialog.youWon)
        assertEquals(GameOverReason.LAST_STANDING, dialog.reason)
    }

    @Test
    fun `eliminated before the end has no winner`() {
        val engine = newEngine()
        val lepen = engine.player(Candidate.LEPEN)

        val dialog = gameOverDialog(engine, lepen.id, winner = null, reason = null)
        assertNull(dialog.winner)
        assertFalse(dialog.youWon)
        assertEquals(GameOverReason.ELIMINATED, dialog.reason)
    }

    @Test
    fun `game over ranking matches the pause ranking sorted by votes`() {
        val engine = newEngine()
        engine.player(Candidate.POUTOU).addVictoryPoints(7)
        engine.player(Candidate.MACRON).addVictoryPoints(7)
        engine.player(Candidate.LEPEN).addVictoryPoints(3)
        val winner = engine.player(Candidate.POUTOU)

        val dialog = gameOverDialog(engine, engine.player(Candidate.LEPEN).id, winner, WinReason.VictoryPointsReached(7))
        assertEquals(pauseDialog(engine).ranking, dialog.ranking)
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
        assertFalse(closed.canBuy)
        assertTrue(open.canBuy)
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
