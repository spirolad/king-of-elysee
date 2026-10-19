package com.example.koe.match

/** Méchanceté levels that each grant a Méchanceté card ("Paliers à 3, 6 et 10"). */
val MechanceteTiers = listOf(3, 6, 10)

/**
 * The highest tier [mechancete] has reached that the player was not rewarded for yet, or null.
 * One reward per end of turn: a jump over two tiers grants the higher one.
 */
fun unrewardedMechanceteTier(mechancete: Int, rewardedTier: Int): Int? =
    MechanceteTiers.lastOrNull { it in (rewardedTier + 1)..mechancete }

/** The next tier above [mechancete]; past the last tier the last one stays the goal. */
fun nextMechanceteTier(mechancete: Int): Int =
    MechanceteTiers.firstOrNull { it > mechancete } ?: MechanceteTiers.last()
