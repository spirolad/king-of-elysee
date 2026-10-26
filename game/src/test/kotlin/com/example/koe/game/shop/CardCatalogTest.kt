package com.example.koe.game.shop

import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import org.junit.Assert.assertEquals
import org.junit.Test

class CardCatalogTest {

    @Test
    fun `CardCatalog deck contains all 42 specified cards`() {
        val deck = CardCatalog.createStandardDeck()
        assertEquals("Expected 42 cards in standard deck, found ${deck.size}", 42, deck.size)
        val actions = CardCatalog.createActionCards()
        val powers = CardCatalog.createPowerCards()
        val wickedness = CardCatalog.createWickednessTiles()
        assertEquals("Expected 11 action cards, found ${actions.size}", 11, actions.size)
        assertEquals("Expected 21 power cards, found ${powers.size}", 21, powers.size)
        assertEquals("Expected 10 wickedness tiles, found ${wickedness.size}", 10, wickedness.size)
    }

    @Test
    fun `Action cards effects - Fausse information, Carte à Strasbourg, Récupération politique`() {
        val buyer = Player(id = PlayerId("b"), name = "Buyer", monster = Monster.GIGA_ZAUR, initialEnergy = 10, initialVictoryPoints = 5)
        val opp1 = Player(id = PlayerId("o1"), name = "Opp1", monster = Monster.ALIENOID, initialEnergy = 4, initialVictoryPoints = 8)
        val opp2 = Player(id = PlayerId("o2"), name = "Opp2", monster = Monster.MEKA_DRAGON, initialEnergy = 2, initialVictoryPoints = 6)
        val all = listOf(buyer, opp1, opp2)

        // Fausse info
        val fausseInfo = CardCatalog.createActionCards().first { it.id == CardId("fausse_information") }
        fausseInfo.effect.apply(buyer, all)
        assertEquals(7, opp1.victoryPoints)
        assertEquals(5, opp2.victoryPoints)

        // Récupération politique (steals from highest VP opp1)
        val recup = CardCatalog.createActionCards().first { it.id == CardId("recuperation_politique") }
        recup.effect.apply(buyer, all)
        assertEquals(2, opp1.energy)
        assertEquals(12, buyer.energy)

        // Carte à Strasbourg
        val strasbourg = CardCatalog.createActionCards().first { it.id == CardId("carte_a_strasbourg") }
        val initialMax = buyer.maxHealth
        strasbourg.effect.apply(buyer, all)
        assertEquals(initialMax + 2, buyer.maxHealth)
    }

    @Test
    fun `Power and Wickedness effects - Censure damage reduction and Rayon antimatière`() {
        val p = Player(id = PlayerId("p"), name = "Player", monster = Monster.CYBER_BUNNY, initialVictoryPoints = 7)
        val censure = CardCatalog.createPowerCards().first { it.id == CardId("censure") }
        p.addCard(censure)
        val dmg = p.damage(4)
        // reduced by 1
        assertEquals(3, dmg)
        assertEquals(7, p.health)

        val rayon = CardCatalog.createWickednessTiles().first { it.id == CardId("rayon_antimatiere") }
        rayon.effect.apply(p, listOf(p))
        // doubled
        assertEquals(14, p.victoryPoints)
    }
}
