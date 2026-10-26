package com.example.koe.game.shop

import com.example.koe.game.player.Player

/**
 * A face-up card with the price a given player would pay for it right now.
 */
data class ShopOffer(
    val card: PowerCard,
    val cost: Int
)

/**
 * Manages the power card shop, deck, and face-up market in King of Tokyo.
 * Adheres to SRP by handling card distribution, sweeping, and transaction validations.
 */
class Shop(
    initialCards: List<PowerCard> = CardCatalog.createStandardDeck().shuffled(),
    val marketSize: Int = DEFAULT_MARKET_SIZE,
    val sweepCost: Int = DEFAULT_SWEEP_COST,
    shuffleDeck: Boolean = false
) {
    private val deck = ArrayDeque(if (shuffleDeck) initialCards.shuffled() else initialCards)
    private val _faceUpCards = mutableListOf<PowerCard>()
    val faceUpCards: List<PowerCard> get() = _faceUpCards.toList()

    init {
        refillMarket()
    }

    fun refillMarket() {
        while (_faceUpCards.size < marketSize && deck.isNotEmpty()) {
            _faceUpCards.add(deck.removeFirst())
        }
    }

    fun calculateCardCost(player: Player, card: PowerCard, allPlayers: List<Player> = emptyList()): Int {
        var cost = card.cost
        val opponentHasLoiLiberticide = allPlayers.any { it.id != player.id && it.isAlive && it.hasCard("loi_liberticide") }
        if (opponentHasLoiLiberticide) {
            cost += 1
        }
        var discount = 0
        if (player.hasCard("clientelisme") || player.hasCard("repaire_malefique")) {
            discount += 1
        }
        if (player.nextCardDiscount > 0) {
            discount += player.nextCardDiscount
        }
        return maxOf(1, cost - discount)
    }

    fun offers(player: Player, allPlayers: List<Player>): List<ShopOffer> =
        faceUpCards.map { ShopOffer(it, calculateCardCost(player, it, allPlayers)) }

    fun sweepFree() {
        _faceUpCards.clear()
        refillMarket()
    }

    /**
     * Discards all current face-up cards and draws new ones if the player can afford the sweep cost.
     */
    fun sweep(player: Player): Boolean {
        if (!player.spendEnergy(sweepCost)) return false
        _faceUpCards.clear()
        refillMarket()
        return true
    }

    /**
     * Executes the purchase of a power card by a player.
     * Verifies affordability, deducts energy, grants the card / applies effect, and refills the market.
     */
    fun buyCard(player: Player, card: PowerCard, allPlayers: List<Player>): CardEffectResult? {
        val index = _faceUpCards.indexOfFirst { it.id == card.id }
        if (index == -1) return null

        val finalCost = calculateCardCost(player, card, allPlayers)
        if (!player.spendEnergy(finalCost)) return null

        if (player.nextCardDiscount > 0) {
            player.nextCardDiscount = 0
        }

        _faceUpCards.removeAt(index)
        refillMarket()

        // Apply card effect
        val result = card.grantTo(player, allPlayers)

        if (card.id.value == "changement_d_assemblee") {
            sweepFree()
        }

        // Trigger Trafic d'influence
        allPlayers.filter { it.id != player.id && it.isAlive && it.hasCard("trafic_d_influence") }.forEach {
            it.addEnergy(1)
        }

        return result
    }

    companion object {
        const val DEFAULT_MARKET_SIZE = 3
        const val DEFAULT_SWEEP_COST = 2
    }
}

/**
 * Applies this card's effect to [player] and keeps it when it is a KEEP card.
 */
fun PowerCard.grantTo(player: Player, allPlayers: List<Player>): CardEffectResult {
    val result = effect.apply(player, allPlayers)
    player.addEnergy(result.energyDelta)
    if (result.healthDelta > 0) player.heal(result.healthDelta)
    if (result.healthDelta < 0) player.damage(-result.healthDelta)
    player.addVictoryPoints(result.victoryPointsDelta)
    if (type == CardType.KEEP) {
        player.addCard(this)
    }
    return result
}
