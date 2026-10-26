package com.example.koe.match.game

import com.example.koe.game.engine.DiceResolutionSummary
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import com.example.koe.match.TurnResultUi
import com.example.koe.match.TweetBannerUi

/** What the current player's dice did this turn: the result pill, the votes delta and the tweet banner. */
class TurnTally {
    private var tallied: PlayerId? = null
    private var startVotes = 0
    private var startFonds = 0
    private var startPopularite = 0
    private var isResolved = false
    private var votesGain = 0
    private var fondsGain = 0
    private var populariteGain = 0
    private var tweets = 0
    private val hits = mutableListOf<TweetHit>()

    private class TweetHit(val target: Player, val damage: Int)

    /** Starts tallying [player]'s turn from their current stats; forgets the previous turn. */
    fun start(player: Player) {
        tallied = player.id
        startVotes = player.victoryPoints
        startFonds = player.energy
        startPopularite = player.health
        isResolved = false
        votesGain = 0
        fondsGain = 0
        populariteGain = 0
        tweets = 0
        hits.clear()
    }

    /** The dice were resolved: points, fonds and popularité gained so far are the dice's. */
    fun resolved(player: Player, summary: DiceResolutionSummary) {
        if (player.id != tallied) return
        isResolved = true
        votesGain = (player.victoryPoints - startVotes).coerceAtLeast(0)
        fondsGain = (player.energy - startFonds).coerceAtLeast(0)
        populariteGain = (player.health - startPopularite).coerceAtLeast(0)
        tweets = summary.attackDamage
    }

    /** A tweet hit; ignored unless [attacker] is the tallied player and the dice were resolved. */
    fun hit(attacker: Player, target: Player, damage: Int) {
        if (attacker.id != tallied || !isResolved) return
        hits.add(TweetHit(target, damage))
    }

    /** null until the dice are resolved. */
    val result: TurnResultUi?
        get() = if (isResolved) {
            TurnResultUi(
                votes = votesGain,
                fonds = fondsGain,
                popularite = populariteGain,
                tweets = tweets,
                targets = hits.map { it.target.candidate }.distinct(),
            )
        } else {
            null
        }

    /** Votes the dice brought, or null when none (or not resolved yet). */
    val votesDelta: Int?
        get() = votesGain.takeIf { isResolved && it > 0 }

    /** Banner for the first tweet target that is not [me], with the popularité it lost; null otherwise. */
    fun tweetBanner(me: PlayerId): TweetBannerUi? =
        hits.firstOrNull { it.target.id != me }?.let {
            TweetBannerUi(target = it.target.candidate, popularityLoss = it.damage)
        }
}
