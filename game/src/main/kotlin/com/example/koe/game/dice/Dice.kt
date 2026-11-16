package com.example.koe.game.dice

/**
 * Represents the 6 possible faces of a King of Tokyo die.
 */
enum class DieFace(val symbol: String, val description: String) {
    ONE("1", "Score 1"),
    TWO("2", "Score 2"),
    THREE("3", "Score 3"),
    ENERGY("⚡", "Energy"),
    ATTACK("⚔", "Attack"),
    HEAL("♥", "Heal");

    val isNumber: Boolean get() = this == ONE || this == TWO || this == THREE
    val numberValue: Int get() = when (this) {
        ONE -> 1
        TWO -> 2
        THREE -> 3
        else -> 0
    }

    override fun toString(): String = symbol
}

/**
 * Represents a single rolled die with its face and whether it is kept for subsequent re-rolls.
 */
data class Die(
    val face: DieFace,
    val isKept: Boolean = false
)

/**
 * Abstraction for rolling dice, adhering to Dependency Inversion (DIP) and testability.
 */
interface DiceRoller {
    fun roll(count: Int): List<DieFace>
    fun rollSingle(): DieFace
}

/**
 * Default random-based dice roller.
 */
class RandomDiceRoller(
    private val random: java.util.Random = java.util.Random()
) : DiceRoller {
    private val faces = DieFace.entries.toTypedArray()

    override fun roll(count: Int): List<DieFace> {
        require(count >= 0) { "Count cannot be negative" }
        return List(count) { rollSingle() }
    }

    override fun rollSingle(): DieFace = faces[random.nextInt(faces.size)]
}
