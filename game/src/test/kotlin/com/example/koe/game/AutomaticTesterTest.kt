package com.example.koe.game

import org.junit.Assert.assertEquals
import org.junit.Test

class AutomaticTesterTest {

    @Test
    fun `AutomaticTester generates and verifies 100 games without invariant failures`() {
        val report = AutomaticTester.run100Games()
        assertEquals("Expected 100 games, got ${report.totalGames}", 100, report.totalGames)
        assertEquals("Expected all 100 games to pass, but ${report.failedGames} failed", 100, report.successfulGames)
        assertEquals(0, report.failedGames)
        assertEquals(100, report.vpWins + report.eliminationWins)
    }
}
