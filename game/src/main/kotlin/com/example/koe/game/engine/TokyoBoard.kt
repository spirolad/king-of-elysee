package com.example.koe.game.engine

import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId

/**
 * Zones within Tokyo.
 */
enum class TokyoZone(val displayName: String) {
    TOKYO_CITY("Tokyo City"),
    TOKYO_BAY("Tokyo Bay")
}

/**
 * Manages the state and rules for occupying Tokyo.
 * Single Responsibility: Manages Tokyo slots, occupant tracking, and attack routing rules.
 */
class TokyoBoard(
    val hasTokyoBay: Boolean = false
) {
    private var cityOccupant: Player? = null
    private var bayOccupant: Player? = null

    val occupants: List<Player>
        get() = listOfNotNull(cityOccupant, bayOccupant).filter { it.isAlive }

    val isCityOccupied: Boolean get() = cityOccupant?.isAlive == true
    val isBayOccupied: Boolean get() = bayOccupant?.isAlive == true
    val isFull: Boolean get() = isCityOccupied && (!hasTokyoBay || isBayOccupied)
    val hasEmptySlot: Boolean get() = !isFull

    fun isInsideTokyo(player: Player): Boolean =
        (cityOccupant?.id == player.id && player.isAlive) ||
        (hasTokyoBay && bayOccupant?.id == player.id && player.isAlive)

    fun getZoneOf(player: Player): TokyoZone? = when {
        cityOccupant?.id == player.id -> TokyoZone.TOKYO_CITY
        hasTokyoBay && bayOccupant?.id == player.id -> TokyoZone.TOKYO_BAY
        else -> null
    }

    /**
     * Attempts to place a monster into an available Tokyo slot.
     * Returns the [TokyoZone] entered, or null if Tokyo is full or player already inside.
     */
    fun enter(player: Player): TokyoZone? {
        if (!player.isAlive || isInsideTokyo(player)) return null

        return when {
            !isCityOccupied -> {
                cityOccupant = player
                TokyoZone.TOKYO_CITY
            }
            hasTokyoBay && !isBayOccupied -> {
                bayOccupant = player
                TokyoZone.TOKYO_BAY
            }
            else -> null
        }
    }

    /**
     * Removes a monster from Tokyo.
     * Returns the [TokyoZone] vacated, or null if the player was not in Tokyo.
     */
    fun leave(player: Player): TokyoZone? {
        return when {
            cityOccupant?.id == player.id -> {
                cityOccupant = null
                TokyoZone.TOKYO_CITY
            }
            bayOccupant?.id == player.id -> {
                bayOccupant = null
                TokyoZone.TOKYO_BAY
            }
            else -> null
        }
    }

    /**
     * Cleans up dead occupants.
     */
    fun cleanupDeadOccupants(): List<Player> {
        val removed = mutableListOf<Player>()
        if (cityOccupant?.isAlive == false) {
            removed.add(cityOccupant!!)
            cityOccupant = null
        }
        if (bayOccupant?.isAlive == false) {
            removed.add(bayOccupant!!)
            bayOccupant = null
        }
        return removed
    }

    /**
     * Determines which players receive attack damage from the attacker.
     * If attacker is outside Tokyo -> all monsters in Tokyo take damage.
     * If attacker is inside Tokyo -> all monsters outside Tokyo take damage.
     */
    fun getTargetsForAttack(attacker: Player, allAlivePlayers: List<Player>): List<Player> {
        val attackerInTokyo = isInsideTokyo(attacker)
        return allAlivePlayers.filter { player ->
            player.id != attacker.id && player.isAlive && (
                if (attackerInTokyo) !isInsideTokyo(player) else isInsideTokyo(player)
            )
        }
    }
}
