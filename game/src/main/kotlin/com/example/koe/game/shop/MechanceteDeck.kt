package com.example.koe.game.shop

/**
 * Méchanceté cards, handed out when a player reaches a tier instead of being sold at the market.
 * A card's tier is its cost (3, 6 or 10).
 */
class MechanceteDeck(
    cards: List<PowerCard> = CardCatalog.createWickednessTiles()
) {
    private val remaining = cards.toMutableList()
    val cards: List<PowerCard> get() = remaining.toList()

    fun choicesFor(tier: Int): List<PowerCard> = remaining.filter { it.cost == tier }

    fun take(card: PowerCard): Boolean = remaining.removeAll { it.id == card.id }

    companion object {
        /** Méchanceté levels that each grant a card. */
        val TIERS = listOf(3, 6, 10)

        /**
         * The highest tier [mechancete] has reached that was not rewarded yet, or null.
         * One reward per check: a jump over two tiers grants the higher one.
         */
        fun unrewardedTier(mechancete: Int, rewardedTier: Int): Int? =
            TIERS.lastOrNull { it in (rewardedTier + 1)..mechancete }
    }
}
