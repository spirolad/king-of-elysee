package com.example.koe.game.engine

import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TokyoBoardTest {

    @Test
    fun `TokyoBoard enter, leave, attack routing`() {
        val board = TokyoBoard(hasTokyoBay = false)
        val p1 = Player(id = PlayerId("p1"), name = "P1", monster = Monster.GIGA_ZAUR)
        val p2 = Player(id = PlayerId("p2"), name = "P2", monster = Monster.MEKA_DRAGON)
        val p3 = Player(id = PlayerId("p3"), name = "P3", monster = Monster.ALIENOID)
        val all = listOf(p1, p2, p3)

        assertFalse(board.isInsideTokyo(p1))
        val zone = board.enter(p1)
        assertEquals(TokyoZone.TOKYO_CITY, zone)
        assertTrue(board.isInsideTokyo(p1))
        assertTrue(board.isFull)

        // Attacker p2 (outside) attacks -> targets should be p1 (in Tokyo)
        val targetsForP2 = board.getTargetsForAttack(p2, all)
        assertEquals(listOf(p1), targetsForP2)

        // Attacker p1 (inside) attacks -> targets should be p2, p3 (outside Tokyo)
        val targetsForP1 = board.getTargetsForAttack(p1, all)
        assertEquals(listOf(p2, p3), targetsForP1)

        // p1 yields/leaves Tokyo
        val leftZone = board.leave(p1)
        assertEquals(TokyoZone.TOKYO_CITY, leftZone)
        assertFalse(board.isInsideTokyo(p1))
        assertTrue(board.hasEmptySlot)
    }
}
