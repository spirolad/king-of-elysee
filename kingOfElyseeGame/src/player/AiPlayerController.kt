package player

import dice.DieFace
import shop.PowerCard

/**
 * An automated AI controller for King of Tokyo players.
 * Makes rational decisions regarding dice retention, yielding Tokyo, and shop purchases.
 */
class AiPlayerController(
    private val healthThresholdToYield: Int = 4
) : PlayerController {

    override fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
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

    override fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int
    ): Boolean {
        // Yield Tokyo if health falls at or below threshold
        return player.health <= healthThresholdToYield
    }

    override fun chooseShopAction(
        player: Player,
        availableCards: List<PowerCard>,
        sweepCost: Int
    ): ShopAction {
        // Prioritize buying affordable cards
        val affordable = availableCards.filter { it.cost <= player.energy }
        if (affordable.isNotEmpty()) {
            val bestCard = affordable.maxByOrNull { it.cost } ?: affordable.first()
            return ShopAction.Buy(bestCard)
        }

        // If player has abundant energy and no good cards, sweep
        if (player.energy >= sweepCost + 4 && availableCards.isNotEmpty()) {
            return ShopAction.Sweep
        }

        return ShopAction.Pass
    }
}
