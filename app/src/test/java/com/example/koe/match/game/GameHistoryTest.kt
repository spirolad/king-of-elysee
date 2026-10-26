package com.example.koe.match.game

import com.example.koe.match.Candidate
import com.example.koe.match.dialog.HistoryEntryUi
import com.example.koe.match.dialog.HistoryKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameHistoryTest {

    private val macron = Candidate.MACRON.toPlayer()
    private val melenchon = Candidate.MELENCHON.toPlayer()

    @Test
    fun entriesAreNewestFirst() {
        val history = GameHistory()

        history.turnStarted(macron, 1)
        history.turnStarted(melenchon, 2)

        assertEquals(
            listOf(
                HistoryEntryUi(round = 2, actor = Candidate.MELENCHON, kind = HistoryKind.TURN_STARTED),
                HistoryEntryUi(round = 1, actor = Candidate.MACRON, kind = HistoryKind.TURN_STARTED),
            ),
            history.entries,
        )
    }

    @Test
    fun eachEventKeepsItsKindActorTargetAmountAndCard() {
        val history = GameHistory()

        history.turnSkipped(macron, 1)
        history.tweet(attacker = macron, target = melenchon, damage = 2, round = 1)
        history.enteredElysee(macron, 1)
        history.yieldedElysee(macron, 1)
        history.boughtCard(macron, "Camouflage", cost = 5, round = 1)
        history.mechanceteCard(macron, "Tir", tier = 3, round = 1)
        history.eliminated(melenchon, 1)

        assertEquals(
            listOf(
                HistoryEntryUi(1, Candidate.MELENCHON, HistoryKind.ELIMINATED),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.MECHANCETE_CARD, amount = 3, cardName = "Tir"),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.BOUGHT_CARD, amount = 5, cardName = "Camouflage"),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.YIELDED_ELYSEE),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.ENTERED_ELYSEE),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.TWEET, target = Candidate.MELENCHON, amount = 2),
                HistoryEntryUi(1, Candidate.MACRON, HistoryKind.TURN_SKIPPED),
            ),
            history.entries,
        )
    }

    @Test
    fun tweetWithoutDamageIsIgnored() {
        val history = GameHistory()

        history.tweet(attacker = macron, target = melenchon, damage = 0, round = 1)

        assertTrue(history.entries.isEmpty())
    }

    @Test
    fun limitDropsTheOldestEntries() {
        val history = GameHistory(limit = 3)

        repeat(4) { history.turnStarted(macron, it + 1) }

        assertEquals(listOf(4, 3, 2), history.entries.map { it.round })
    }
}
