package com.example.koe.game.engine

import com.example.koe.game.dice.DieFace
import org.junit.Assert.assertEquals
import org.junit.Test

class DiceResolverTest {

    @Test
    fun `DiceResolver 3-of-a-kind scoring`() {
        // Three 1s -> 1 VP
        val res1 = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.ENERGY, DieFace.ENERGY, DieFace.ATTACK), isInTokyo = false)
        assertEquals(1, res1.victoryPoints)
        assertEquals(2, res1.energy)
        assertEquals(1, res1.attackDamage)

        // Four 2s -> 2 + 1 = 3 VP
        val res2 = DiceResolver.resolve(listOf(DieFace.TWO, DieFace.TWO, DieFace.TWO, DieFace.TWO, DieFace.HEAL, DieFace.HEAL), isInTokyo = false)
        assertEquals(3, res2.victoryPoints)
        assertEquals(2, res2.actualHeal)

        // Five 3s -> 3 + 2 = 5 VP
        val res3 = DiceResolver.resolve(listOf(DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.THREE, DieFace.ATTACK), isInTokyo = false)
        assertEquals(5, res3.victoryPoints)
        assertEquals(1, res3.attackDamage)
    }

    @Test
    fun `DiceResolver Tokyo heal restriction`() {
        val resOutside = DiceResolver.resolve(listOf(DieFace.HEAL, DieFace.HEAL), isInTokyo = false)
        assertEquals(2, resOutside.actualHeal)

        val resInside = DiceResolver.resolve(listOf(DieFace.HEAL, DieFace.HEAL), isInTokyo = true)
        assertEquals(2, resInside.heartsRolled)
        assertEquals(0, resInside.actualHeal)
    }

    @Test
    fun `Méchanceté from three ones and three twos`() {
        val oneTrio = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.ENERGY, DieFace.ENERGY, DieFace.ENERGY), isInTokyo = false)
        assertEquals(1, oneTrio.mechanceteGained)

        val twoTrio = DiceResolver.resolve(listOf(DieFace.TWO, DieFace.TWO, DieFace.TWO, DieFace.ENERGY, DieFace.ENERGY, DieFace.ENERGY), isInTokyo = false)
        assertEquals(2, twoTrio.mechanceteGained)

        val both = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.TWO, DieFace.TWO, DieFace.TWO), isInTokyo = false)
        assertEquals(3, both.mechanceteGained)

        val fourOnes = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.ONE, DieFace.ENERGY, DieFace.ENERGY), isInTokyo = false)
        assertEquals(1, fourOnes.mechanceteGained)

        val pairs = DiceResolver.resolve(listOf(DieFace.ONE, DieFace.ONE, DieFace.TWO, DieFace.TWO, DieFace.ENERGY, DieFace.ATTACK), isInTokyo = false)
        assertEquals(0, pairs.mechanceteGained)
    }
}
