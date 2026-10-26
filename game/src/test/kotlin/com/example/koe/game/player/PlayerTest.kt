package com.example.koe.game.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTest {

    @Test
    fun `Player health clamping and damage`() {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR, maxHealth = 10, initialHealth = 10)
        assertTrue(player.isAlive)
        val dealt = player.damage(4)
        assertEquals(4, dealt)
        assertEquals(6, player.health)
        val overkill = player.damage(10)
        assertEquals(6, overkill)
        assertEquals(0, player.health)
        assertFalse(player.isAlive)
    }

    @Test
    fun `Player healing does not exceed maxHealth`() {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR, maxHealth = 10, initialHealth = 6)
        val healed = player.heal(3)
        assertEquals(3, healed)
        assertEquals(9, player.health)
        val overHealed = player.heal(5)
        assertEquals(1, overHealed)
        assertEquals(10, player.health)
    }

    @Test
    fun `Player energy and VP mutations`() {
        val player = Player(id = PlayerId("p1"), name = "Giga", monster = Monster.GIGA_ZAUR)
        player.addEnergy(5)
        assertEquals(5, player.energy)
        assertTrue(player.spendEnergy(3))
        assertEquals(2, player.energy)
        assertFalse(player.spendEnergy(5))
        assertEquals(2, player.energy)

        player.addVictoryPoints(15)
        assertEquals(15, player.victoryPoints)
        player.deductVictoryPoints(5)
        assertEquals(10, player.victoryPoints)
    }
}
