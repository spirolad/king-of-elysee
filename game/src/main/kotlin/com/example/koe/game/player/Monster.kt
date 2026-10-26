package com.example.koe.game.player

/**
 * Unique identifier for a player in the game.
 */
@JvmInline
value class PlayerId(val value: String) {
    override fun toString(): String = value
}

/**
 * Standard monsters available in King of Tokyo.
 */
enum class Monster(val displayName: String) {
    GIGA_ZAUR("Giga Zaur"),
    MEKA_DRAGON("Meka Dragon"),
    ALIENOID("Alienoid"),
    CYBER_BUNNY("Cyber Bunny"),
    THE_KING("The King"),
    KRAKEN("Kraken")
}
