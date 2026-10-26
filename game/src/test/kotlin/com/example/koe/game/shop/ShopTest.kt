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

    @Test
    fun `offers carry the price each player pays`() {
        val testCards = listOf(
            PowerCard(CardId("t1"), "Test One", 3, CardType.KEEP, "Test"),
            PowerCard(CardId("t2"), "Test Two", 3, CardType.KEEP, "Test"),
            PowerCard(CardId("t3"), "Test Three", 3, CardType.KEEP, "Test")
        )
        val shop = Shop(initialCards = testCards, marketSize = 3, sweepCost = 2)

        // Clientelisme: purchases cost 1 less
        val discounted = Player(id = PlayerId("discounted"), name = "Discounted", monster = Monster.KRAKEN, initialEnergy = 5)
        discounted.addCard(CardCatalog.createPowerCards().first { it.id == CardId("clientelisme") })
        assertEquals(listOf(2, 2, 2), shop.offers(discounted, listOf(discounted)).map { it.cost })

        // An opponent with Loi liberticide adds 1 to every purchase
        val plain = Player(id = PlayerId("plain"), name = "Plain", monster = Monster.ALIENOID, initialEnergy = 5)
        val rival = Player(id = PlayerId("rival"), name = "Rival", monster = Monster.MEKA_DRAGON)
        rival.addCard(CardCatalog.createPowerCards().first { it.id == CardId("loi_liberticide") })
        assertEquals(listOf(4, 4, 4), shop.offers(plain, listOf(plain, rival)).map { it.cost })
    }
}
