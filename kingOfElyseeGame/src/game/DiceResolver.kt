package game

import dice.DieFace

/**
 * Summary of calculated effects from a resolved dice pool.
 */
data class DiceResolutionSummary(
    val victoryPoints: Int,
    val energy: Int,
    val attackDamage: Int,
    val heartsRolled: Int,
    val actualHeal: Int
)

/**
 * Encapsulates the rules for calculating results from rolled dice.
 * Adheres to SRP by separating dice arithmetic and rules from the game loop.
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

        val energy = counts[DieFace.ENERGY] ?: 0
        val attack = counts[DieFace.ATTACK] ?: 0
        val hearts = counts[DieFace.HEAL] ?: 0
        // Monsters in Tokyo cannot heal via dice hearts
        val actualHeal = if (isInTokyo) 0 else hearts

        return DiceResolutionSummary(
            victoryPoints = vp,
            energy = energy,
            attackDamage = attack,
            heartsRolled = hearts,
            actualHeal = actualHeal
        )
    }
}
