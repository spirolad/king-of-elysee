package com.example.koe.match.game

import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.delay

/** Wraps an AI controller so each of its decisions waits [delayMillis], letting the player follow AI turns. */
class PacedAiController(
    private val delegate: PlayerController = AiPlayerController(),
    private val delayMillis: Long,
) : PlayerController {

    override suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {
        delay(delayMillis)
        delegate.awaitFirstRoll(player, maxRolls)
    }

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>,
    ): List<Int> {
        delay(delayMillis)
        return delegate.chooseDiceToKeep(player, rollNumber, maxRolls, currentDice, currentlyKept)
    }

    override suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int,
    ): Boolean {
        delay(delayMillis)
        return delegate.shouldYieldTokyo(player, attacker, damage)
    }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int,
    ): ShopAction {
        delay(delayMillis)
        return delegate.chooseShopAction(player, offers, sweepCost)
    }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>,
    ): PowerCard {
        delay(delayMillis)
        return delegate.chooseMechanceteCard(player, tier, choices)
    }
}
