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
    data class Roll(
        val maxRolls: Int,
        val answer: CompletableDeferred<Unit>,
    ) : HumanDecision

    data class KeepDice(
        val rollNumber: Int,
        val maxRolls: Int,
        val currentlyKept: List<Boolean>,
        val answer: CompletableDeferred<List<Int>>,
    ) : HumanDecision

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

    override suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {
        ask<Unit> { HumanDecision.Roll(maxRolls, it) }
    }

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>,
    ): List<Int> = ask<List<Int>> { HumanDecision.KeepDice(rollNumber, maxRolls, currentlyKept, it) }

    override suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int,
    ): Boolean = ask<Boolean> { HumanDecision.Yield(attacker, damage, it) }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int,
    ): ShopAction = ask<ShopAction> { HumanDecision.Shop(offers, sweepCost, it) }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>,
    ): PowerCard = ask<PowerCard> { HumanDecision.Mechancete(tier, choices, it) }

    /** Publishes the decision built around a fresh answer, then suspends until the UI completes it. */
    private suspend fun <T> ask(create: (CompletableDeferred<T>) -> HumanDecision): T {
        val answer = CompletableDeferred<T>()
        onDecision(create(answer))
        return try {
            answer.await()
        } finally {
            onDecision(null)
        }
    }
}
