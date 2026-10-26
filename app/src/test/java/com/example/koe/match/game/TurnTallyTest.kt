package com.example.koe.match.game

import com.example.koe.game.engine.DiceResolutionSummary
import com.example.koe.game.player.Player
import com.example.koe.match.Candidate
import com.example.koe.match.TurnResultUi
import com.example.koe.match.TweetBannerUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TurnTallyTest {

    private fun summary(
        victoryPoints: Int = 0,
        energy: Int = 0,
        attackDamage: Int = 0,
    ) = DiceResolutionSummary(
        victoryPoints = victoryPoints,
        energy = energy,
        attackDamage = attackDamage,
        heartsRolled = 0,
        actualHeal = 0,
        mechanceteGained = 0,
    )

    @Test
    fun `nothing is tallied before start`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val other = Candidate.LEPEN.toPlayer()

        tally.resolved(me, summary(victoryPoints = 3, attackDamage = 2))
        tally.hit(me, other, 2)

        assertNull(tally.result)
        assertNull(tally.votesDelta)
        assertNull(tally.tweetBanner(me.id))
    }

    @Test
    fun `result is null after start until the dice are resolved`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()

        tally.start(me)
        me.addVictoryPoints(3)
        assertNull(tally.result)

        tally.resolved(me, summary())
        assertEquals(
            TurnResultUi(votes = 3, fonds = 0, popularite = 0, tweets = 0, targets = emptyList()),
            tally.result,
        )
    }

    @Test
    fun `gains are the changes since start and tweets come from the summary`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        me.addVictoryPoints(1)
        me.addEnergy(2)
        me.damage(3)

        tally.start(me)
        me.addVictoryPoints(3)
        me.addEnergy(1)
        me.damage(4)
        me.heal(6)
        tally.resolved(me, summary(victoryPoints = 3, energy = 1, attackDamage = 2))

        assertEquals(
            TurnResultUi(votes = 3, fonds = 1, popularite = 2, tweets = 2, targets = emptyList()),
            tally.result,
        )
    }

    @Test
    fun `losses count as zero gain`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        me.addVictoryPoints(5)

        tally.start(me)
        me.deductVictoryPoints(2)
        tally.resolved(me, summary())

        assertEquals(0, tally.result?.votes)
        assertNull(tally.votesDelta)
    }

    @Test
    fun `votes delta is null when the dice brought no votes`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()

        tally.start(me)
        tally.resolved(me, summary())

        assertNull(tally.votesDelta)
    }

    @Test
    fun `votes delta is the votes the dice brought`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()

        tally.start(me)
        me.addVictoryPoints(3)
        tally.resolved(me, summary(victoryPoints = 3))

        assertEquals(3, tally.votesDelta)
    }

    @Test
    fun `hits from another attacker are ignored`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val other = Candidate.LEPEN.toPlayer()
        val target = Candidate.POUTOU.toPlayer()

        tally.start(me)
        tally.resolved(me, summary(attackDamage = 2))
        tally.hit(other, target, 2)

        assertEquals(
            TurnResultUi(votes = 0, fonds = 0, popularite = 0, tweets = 2, targets = emptyList()),
            tally.result,
        )
        assertNull(tally.tweetBanner(me.id))
    }

    @Test
    fun `hits before the dice are resolved are ignored`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val target = Candidate.POUTOU.toPlayer()

        tally.start(me)
        tally.hit(me, target, 2)
        tally.resolved(me, summary())

        assertEquals(emptyList<Candidate>(), tally.result?.targets)
        assertNull(tally.tweetBanner(me.id))
    }

    @Test
    fun `targets are distinct in hit order`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val poutou = Candidate.POUTOU.toPlayer()
        val lepen = Candidate.LEPEN.toPlayer()

        tally.start(me)
        tally.resolved(me, summary(attackDamage = 6))
        tally.hit(me, poutou, 1)
        tally.hit(me, lepen, 2)
        tally.hit(me, poutou, 3)

        assertEquals(listOf(Candidate.POUTOU, Candidate.LEPEN), tally.result?.targets)
    }

    @Test
    fun `tweet banner skips a hit on me and returns the next target`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val lepen = Candidate.LEPEN.toPlayer()

        tally.start(me)
        tally.resolved(me, summary(attackDamage = 5))
        tally.hit(me, me, 2)
        tally.hit(me, lepen, 3)

        assertEquals(
            TweetBannerUi(target = Candidate.LEPEN, popularityLoss = 3),
            tally.tweetBanner(me.id),
        )
    }

    @Test
    fun `start again forgets the previous turn`() {
        val tally = TurnTally()
        val me = Candidate.MACRON.toPlayer()
        val lepen = Candidate.LEPEN.toPlayer()
        val next = Candidate.POUTOU.toPlayer()

        tally.start(me)
        me.addVictoryPoints(2)
        tally.resolved(me, summary(victoryPoints = 2, attackDamage = 2))
        tally.hit(me, lepen, 2)

        tally.start(next)
        assertNull(tally.result)
        assertNull(tally.votesDelta)
        assertNull(tally.tweetBanner(me.id))

        tally.resolved(me, summary(victoryPoints = 2, attackDamage = 2))
        assertNull(tally.result)
    }
}
