package com.example.koe.match.game

import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import kotlinx.coroutines.CompletableDeferred

/** A decision the engine is waiting for from the human player. */
sealed interface HumanDecision {
    data class Shop(
        val offers: List<ShopOffer>,
        val sweepCost: Int,
        val answer: CompletableDeferred<ShopAction>,
    ) : HumanDecision

    data class Yield(
        val attacker: Player,
        val damage: Int,
        val answer: CompletableDeferred<Boolean>,
    ) : HumanDecision

    data class Mechancete(
        val tier: Int,
        val choices: List<PowerCard>,
        val answer: CompletableDeferred<PowerCard>,
    ) : HumanDecision
}

/** Suspends the engine on the human's decisions until the UI answers them through [onDecision]'s deferred. */
class HumanPlayerController(private val onDecision: (HumanDecision?) -> Unit) : PlayerController {

    // Until the dice row is interactive, the first roll is kept.
    override suspend fun awaitFirstRoll(player: Player) {}

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>,
    ): List<Int> = currentDice.indices.toList()

    override suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int,
    ): Boolean {
        val decision = HumanDecision.Yield(attacker, damage, CompletableDeferred())
        onDecision(decision)
        return try {
            decision.answer.await()
        } finally {
            onDecision(null)
        }
    }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int,
    ): ShopAction {
        val decision = HumanDecision.Shop(offers, sweepCost, CompletableDeferred())
        onDecision(decision)
        return try {
            decision.answer.await()
        } finally {
            onDecision(null)
        }
    }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>,
    ): PowerCard {
        val decision = HumanDecision.Mechancete(tier, choices, CompletableDeferred())
        onDecision(decision)
        return try {
            decision.answer.await()
        } finally {
            onDecision(null)
        }
    }
}
