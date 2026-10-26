package com.example.koe.game.player

import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.PowerCard

/**
 * Encapsulates the stats and mutable state of a monster/player in King of Tokyo.
 * Adheres to SRP by managing player attributes, life cycle, and card inventory.
 */
class Player(
    val id: PlayerId,
    val name: String,
    val monster: Monster,
    maxHealth: Int = DEFAULT_MAX_HEALTH,
    initialHealth: Int = maxHealth,
    initialEnergy: Int = 0,
    initialVictoryPoints: Int = 0
) {
    var maxHealth: Int = maxHealth
        private set

    var health: Int = initialHealth.coerceIn(0, maxHealth)
        private set

    var energy: Int = initialEnergy.coerceAtLeast(0)
        private set

    var victoryPoints: Int = initialVictoryPoints.coerceAtLeast(0)
        private set

    var noEnergyNextTurn: Boolean = false
    var skipNextTurn: Boolean = false
    var nextCardDiscount: Int = 0
    var hasActionImmunity: Boolean = false

    private val _cards = mutableListOf<PowerCard>()
    val cards: List<PowerCard> get() = _cards.toList()

    val isAlive: Boolean get() = health > 0

    fun hasCard(cardId: String): Boolean = _cards.any { it.id.value == cardId }
    fun hasCard(cardId: com.example.koe.game.shop.CardId): Boolean = _cards.any { it.id == cardId }

    fun increaseMaxHealth(amount: Int) {
        if (amount > 0) {
            maxHealth += amount
        }
    }

    fun increaseMaxHealthTo(newMax: Int) {
        if (newMax > maxHealth) {
            maxHealth = newMax
        }
    }

    // Additional dice granted by keep cards
    val extraDiceCount: Int
        get() = _cards.sumOf { it.extraDice }

    fun damage(amount: Int): Int {
        if (amount <= 0 || !isAlive) return 0
        var effectiveDamage = amount
        if (hasCard("censure") && effectiveDamage > 1) {
            effectiveDamage = maxOf(1, effectiveDamage - 1)
        }
        if (hasCard("gerontocratie") && health >= 6) {
            effectiveDamage = maxOf(0, effectiveDamage - 1)
        }
        if (hasCard("armor_plating")) {
            effectiveDamage = maxOf(0, effectiveDamage - 1)
        }
        if (effectiveDamage <= 0) return 0
        val actualDamage = minOf(health, effectiveDamage)
        health -= actualDamage
        return actualDamage
    }

    fun heal(amount: Int): Int {
        if (amount <= 0 || !isAlive) return 0
        val healed = minOf(maxHealth - health, amount)
        health += healed
        return healed
    }

    fun addEnergy(amount: Int) {
        if (amount > 0) {
            val bonus = if (hasCard("paradis_fiscal")) 1 else 0
            energy += (amount + bonus)
        }
    }

    fun spendEnergy(amount: Int): Boolean {
        if (amount <= 0) return true
        if (energy >= amount) {
            energy -= amount
            return true
        }
        return false
    }

    fun addVictoryPoints(amount: Int) {
        if (amount > 0) {
            victoryPoints += amount
        }
    }

    fun deductVictoryPoints(amount: Int): Int {
        if (amount <= 0) return 0
        val deducted = minOf(victoryPoints, amount)
        victoryPoints -= deducted
        return deducted
    }

    fun addCard(card: PowerCard) {
        if (card.type == CardType.KEEP) {
            _cards.add(card)
        }
    }

    fun removeCard(card: PowerCard): Boolean = _cards.remove(card)

    override fun toString(): String =
        "$name [${monster.displayName}] (❤️ $health/$maxHealth, ⚡ $energy, 🏆 $victoryPoints)"

    companion object {
        const val DEFAULT_MAX_HEALTH = 10
        const val WINNING_VICTORY_POINTS = 20
    }
}
