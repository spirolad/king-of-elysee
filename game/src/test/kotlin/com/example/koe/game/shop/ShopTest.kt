package com.example.koe.game.shop

import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopTest {

    @Test
    fun `Shop purchase and sweep`() {
        val testCards = listOf(
            PowerCard(CardId("c1"), "Energy Drink", 2, CardType.DISCARD, "Gain 3 energy", { _, _ -> CardEffectResult("Boost", energyDelta = 3) }),
            PowerCard(CardId("c2"), "Big Armor", 4, CardType.KEEP, "Armor test"),
            PowerCard(CardId("c3"), "Nuclear Blast", 5, CardType.DISCARD, "Nuke"),
            PowerCard(CardId("c4"), "Spare Card", 1, CardType.DISCARD, "Spare")
        )
        val shop = Shop(initialCards = testCards, marketSize = 3, sweepCost = 2)
        val buyer = Player(id = PlayerId("buyer"), name = "Buyer", monster = Monster.KRAKEN, initialEnergy = 5)

        val cardToBuy = shop.faceUpCards.first { it.id == CardId("c1") }
        val result = shop.buyCard(buyer, cardToBuy, listOf(buyer))
        assertNotNull(result)
        // spent 2, gained 3
        assertEquals(5 - 2 + 3, buyer.energy)
        // refilled with c4
        assertEquals(3, shop.faceUpCards.size)

        // Sweep shop
        val swept = shop.sweep(buyer)
        assertTrue(swept)
        // spent 2
        assertEquals(6 - 2, buyer.energy)
    }
}
