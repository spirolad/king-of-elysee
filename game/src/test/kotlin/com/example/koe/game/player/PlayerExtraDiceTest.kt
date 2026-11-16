package com.example.koe.game.player

import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlayerExtraDiceTest {

    private fun newPlayer(): Player =
        Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)

    private fun catalogCard(id: String) =
        (CardCatalog.createStandardDeck() + CardCatalog.createWickednessTiles()).first { it.id == CardId(id) }

    @Test
    fun `cybercerveau grants exactly one extra die`() {
        val player = newPlayer()
        player.addCard(catalogCard("cybercerveau"))
        assertEquals(1, player.extraDiceCount)

        assertEquals(0, newPlayer().extraDiceCount)
    }

    @Test
    fun `reading extraDiceCount does not heal through regeneration_totale`() {
        val player = newPlayer()
        assertEquals(10, player.maxHealth)
        player.addCard(catalogCard("regeneration_totale"))
        player.damage(4)
        assertEquals(6, player.health)

        player.extraDiceCount
        player.extraDiceCount

        assertEquals(6, player.health)
        assertEquals(10, player.maxHealth)
    }

    @Test
    fun `reading extraDiceCount does not re-arm immunite_parlementaire`() {
        val player = newPlayer()
        player.addCard(catalogCard("immunite_parlementaire"))
        player.hasActionImmunity = false

        player.extraDiceCount

        assertFalse(player.hasActionImmunity)
    }
}
