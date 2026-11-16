package com.example.koe.game.player

import com.example.koe.game.shop.PowerCard

/**
 * A card bought at the market: what was paid and in which round.
 */
data class CardPurchase(
    val card: PowerCard,
    val cost: Int,
    val round: Int
)
