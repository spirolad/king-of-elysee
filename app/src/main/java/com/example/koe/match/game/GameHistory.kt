package com.example.koe.match.game

import com.example.koe.game.player.Player
import com.example.koe.match.Candidate
import com.example.koe.match.dialog.HistoryEntryUi
import com.example.koe.match.dialog.HistoryKind

/** The match's notable events, newest first, for the history popup. */
class GameHistory(private val limit: Int = 100) {
    // Replaced on every event, so a list handed out earlier never changes under its holder.
    private var log: List<HistoryEntryUi> = emptyList()

    val entries: List<HistoryEntryUi> get() = log

    fun turnStarted(player: Player, round: Int) = add(player, round, HistoryKind.TURN_STARTED)

    fun turnSkipped(player: Player, round: Int) = add(player, round, HistoryKind.TURN_SKIPPED)

    /** A tweet from [attacker] that hurt [target]; a hit that did no damage is not news. */
    fun tweet(attacker: Player, target: Player, damage: Int, round: Int) {
        if (damage <= 0) return
        add(attacker, round, HistoryKind.TWEET, target = target.candidate, amount = damage)
    }

    fun enteredElysee(player: Player, round: Int) = add(player, round, HistoryKind.ENTERED_ELYSEE)

    fun yieldedElysee(player: Player, round: Int) = add(player, round, HistoryKind.YIELDED_ELYSEE)

    fun boughtCard(player: Player, cardName: String, cost: Int, round: Int) =
        add(player, round, HistoryKind.BOUGHT_CARD, amount = cost, cardName = cardName)

    fun mechanceteCard(player: Player, cardName: String, tier: Int, round: Int) =
        add(player, round, HistoryKind.MECHANCETE_CARD, amount = tier, cardName = cardName)

    fun eliminated(player: Player, round: Int) = add(player, round, HistoryKind.ELIMINATED)

    /** What [player]'s dice brought; ignored when they brought nothing visible. */
    fun diceResult(player: Player, votes: Int, fonds: Int, popularite: Int, tweets: Int, round: Int) {
        if (votes == 0 && fonds == 0 && popularite == 0 && tweets == 0) return
        add(
            player,
            round,
            HistoryKind.DICE_RESULT,
            amount = tweets.takeIf { it > 0 },
            votes = votes,
            fonds = fonds,
            popularite = popularite,
        )
    }

    private fun add(
        player: Player,
        round: Int,
        kind: HistoryKind,
        target: Candidate? = null,
        amount: Int? = null,
        cardName: String? = null,
        votes: Int = 0,
        fonds: Int = 0,
        popularite: Int = 0,
    ) {
        val entry = HistoryEntryUi(
            round = round,
            actor = player.candidate,
            kind = kind,
            target = target,
            amount = amount,
            cardName = cardName,
            votes = votes,
            fonds = fonds,
            popularite = popularite,
        )
        log = (listOf(entry) + log).take(limit)
    }
}
