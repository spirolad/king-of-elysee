package com.example.koe.game.engine

import com.example.koe.game.dice.DieFace

data class DiceResolutionSummary(
    val victoryPoints: Int,
    val energy: Int,
    val attackDamage: Int,
    val heartsRolled: Int,
    val actualHeal: Int,
    val mechanceteGained: Int,
    val wickednessPoints: Int = 0
)

/**
 * Encapsulates the rules for calculating results from rolled dice.
 */
object DiceResolver {

    /**
     * Resolves the dice faces into points, energy, attacks, and heals.
     */
    fun resolve(
        dice: List<DieFace>,
        isInTokyo: Boolean
    ): DiceResolutionSummary {
        val counts = dice.groupingBy { it }.eachCount()

        // Calculate victory points from number dice (3-of-a-kind rule)
        var vp = 0
        for (numFace in listOf(DieFace.ONE, DieFace.TWO, DieFace.THREE)) {
            val count = counts[numFace] ?: 0
            if (count >= 3) {
                val base = numFace.numberValue
                val extra = count - 3
                vp += base + extra
            }
        }

        // Wickedness points (Dark Edition rule) are tallied but drive nothing; Méchanceté below is what grants cards.
        var wickedness = 0
        val ones = counts[DieFace.ONE] ?: 0
        if (ones >= 3) {
            wickedness += 2 + (ones - 3)
        }
        val twos = counts[DieFace.TWO] ?: 0
        if (twos >= 3) {
            wickedness += 1 + (twos - 3)
        }

        val energy = counts[DieFace.ENERGY] ?: 0
        val attack = counts[DieFace.ATTACK] ?: 0
        val hearts = counts[DieFace.HEAL] ?: 0
        // Monsters in Tokyo cannot heal via dice hearts
        val actualHeal = if (isInTokyo) 0 else hearts
        // Méchanceté: 3+ [1] give +1, 3+ [2] give +2 (both can apply)
        val mechanceteGained =
            (if ((counts[DieFace.ONE] ?: 0) >= 3) 1 else 0) +
                (if ((counts[DieFace.TWO] ?: 0) >= 3) 2 else 0)

        return DiceResolutionSummary(
            victoryPoints = vp,
            energy = energy,
            attackDamage = attack,
            heartsRolled = hearts,
            actualHeal = actualHeal,
            mechanceteGained = mechanceteGained,
            wickednessPoints = wickedness
        )
    }
}
