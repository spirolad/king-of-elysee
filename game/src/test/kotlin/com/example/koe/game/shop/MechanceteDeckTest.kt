package com.example.koe.game.shop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MechanceteDeckTest {

    @Test
    fun `unrewardedTier reports the highest tier reached and not yet rewarded`() {
        assertNull(MechanceteDeck.unrewardedTier(0, 0))
        assertEquals(3, MechanceteDeck.unrewardedTier(3, 0))
        assertEquals(6, MechanceteDeck.unrewardedTier(7, 0))
        assertEquals(6, MechanceteDeck.unrewardedTier(7, 3))
        assertNull(MechanceteDeck.unrewardedTier(5, 3))
        assertEquals(10, MechanceteDeck.unrewardedTier(12, 6))
        assertNull(MechanceteDeck.unrewardedTier(12, 10))
    }

    @Test
    fun `choicesFor lists the cards of a tier and take removes the picked one`() {
        val deck = MechanceteDeck()
        assertEquals(4, deck.choicesFor(3).size)
        assertTrue(deck.choicesFor(3).all { it.cost == 3 })
        assertEquals(2, deck.choicesFor(10).size)

        val picked = deck.choicesFor(3).first()
        assertTrue(deck.take(picked))
        assertFalse(deck.choicesFor(3).any { it.id == picked.id })
        assertEquals(9, deck.cards.size)
    }
}
