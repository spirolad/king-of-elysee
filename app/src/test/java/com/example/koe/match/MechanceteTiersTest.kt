package com.example.koe.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MechanceteTiersTest {

    @Test
    fun belowFirstTierGrantsNothing() {
        assertNull(unrewardedMechanceteTier(mechancete = 2, rewardedTier = 0))
    }

    @Test
    fun reachingATierGrantsIt() {
        assertEquals(3, unrewardedMechanceteTier(mechancete = 3, rewardedTier = 0))
        assertEquals(6, unrewardedMechanceteTier(mechancete = 7, rewardedTier = 3))
    }

    @Test
    fun aRewardedTierIsNotGrantedTwice() {
        assertNull(unrewardedMechanceteTier(mechancete = 5, rewardedTier = 3))
        assertNull(unrewardedMechanceteTier(mechancete = 12, rewardedTier = 10))
    }

    @Test
    fun jumpingOverTiersGrantsTheHighest() {
        assertEquals(6, unrewardedMechanceteTier(mechancete = 6, rewardedTier = 0))
    }

    @Test
    fun nextTierIsTheFirstAboveTheLevel() {
        assertEquals(3, nextMechanceteTier(2))
        assertEquals(6, nextMechanceteTier(3))
        assertEquals(10, nextMechanceteTier(10))
    }
}
