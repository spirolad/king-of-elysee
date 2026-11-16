package com.example.koe.match.game

import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.delay

/**
 * Wraps an AI controller so the player can follow AI turns. Each decision waits [thinkMillis], except the
 * first shop decision of a turn, which waits [resultMillis] so the turn's result stays readable.
 * The first shop decision is detected from the start of the turn, [awaitFirstRoll].
 * While [skipDelays] is true, as when a restored match replays the human's answers, no decision waits.
 */
class PacedAiController(
    private val delegate: PlayerController = AiPlayerController(),
    private val thinkMillis: Long,
    private val resultMillis: Long,
    private val skipDelays: () -> Boolean = { false },
) : PlayerController {

    private var isFirstShopDecision = true

    override suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {
        isFirstShopDecision = true
        if (!skipDelays()) delay(thinkMillis)
        delegate.awaitFirstRoll(player, maxRolls)
    }

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>,
    ): List<Int> {
        if (!skipDelays()) delay(thinkMillis)
        return delegate.chooseDiceToKeep(player, rollNumber, maxRolls, currentDice, currentlyKept)
    }

    override suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int,
    ): Boolean {
        if (!skipDelays()) delay(thinkMillis)
        return delegate.shouldYieldTokyo(player, attacker, damage)
    }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int,
    ): ShopAction {
        val wait = if (isFirstShopDecision) resultMillis else thinkMillis
        isFirstShopDecision = false
        if (!skipDelays()) delay(wait)
        return delegate.chooseShopAction(player, offers, sweepCost)
    }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>,
    ): PowerCard {
        if (!skipDelays()) delay(thinkMillis)
        return delegate.chooseMechanceteCard(player, tier, choices)
    }
}
