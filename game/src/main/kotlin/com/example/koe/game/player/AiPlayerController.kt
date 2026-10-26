package com.example.koe.game.player

import com.example.koe.game.dice.DieFace
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer

/**
 * An automated AI controller for King of Tokyo players.
 * Makes rational decisions regarding dice retention, yielding Tokyo, and shop purchases.
 */
class AiPlayerController(
    private val healthThresholdToYield: Int = 4
) : PlayerController {

    override suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>
    ): List<Int> {
        val keptIndices = mutableListOf<Int>()
        val counts = currentDice.groupingBy { it }.eachCount()

        currentDice.forEachIndexed { index, face ->
            val shouldKeep = when (face) {
                DieFace.HEAL -> player.health < player.maxHealth
                DieFace.ENERGY -> true
                DieFace.ATTACK -> true
                DieFace.ONE, DieFace.TWO, DieFace.THREE -> (counts[face] ?: 0) >= 2
            }
            if (shouldKeep) {
                keptIndices.add(index)
            }
        }
        return keptIndices
    }

    override suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int
    ): Boolean {
        // Yield Tokyo if health falls at or below threshold
        return player.health <= healthThresholdToYield
    }

    override suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int
    ): ShopAction {
        // Prioritize buying the most expensive card the player can afford
        val affordable = offers.filter { it.cost <= player.energy }
        if (affordable.isNotEmpty()) {
            val best = affordable.maxByOrNull { it.cost } ?: affordable.first()
            return ShopAction.Buy(best.card)
        }

        // If player has abundant energy and no good cards, sweep
        if (player.energy >= sweepCost + 4 && offers.isNotEmpty()) {
            return ShopAction.Sweep
        }

        return ShopAction.Pass
    }

    override suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>
    ): PowerCard {
        // Kept cards keep paying off every turn, so prefer them over one-shot cards
        return choices.firstOrNull { it.type == CardType.KEEP } ?: choices.first()
    }
}
