package shop

import player.Player

/**
 * Type of power card in King of Tokyo.
 */
enum class CardType {
    KEEP,
    DISCARD
}

/**
 * Unique identifier for cards.
 */
@JvmInline
value class CardId(val value: String)

/**
 * Strategy interface for executing card effects (Open/Closed Principle).
 */
fun interface CardEffect {
    fun apply(buyer: Player, allPlayers: List<Player>): CardEffectResult
}

/**
 * Outcome resulting from applying a card effect.
 */
data class CardEffectResult(
    val message: String,
    val energyDelta: Int = 0,
    val healthDelta: Int = 0,
    val victoryPointsDelta: Int = 0,
    val extraDiceCount: Int = 0
)

/**
 * Represents a Power Card in the King of Tokyo shop.
 */
data class PowerCard(
    val id: CardId,
    val name: String,
    val cost: Int,
    val type: CardType,
    val description: String,
    val effect: CardEffect = CardEffect { _, _ -> CardEffectResult("No additional effect") }
) {
    override fun toString(): String = "[$name (Cost: $cost ⚡, Type: $type) - $description]"
}
