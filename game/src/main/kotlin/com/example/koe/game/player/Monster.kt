package com.example.koe.game.player

@JvmInline
value class PlayerId(val value: String) {
    override fun toString(): String = value
}

enum class Monster(val displayName: String) {
    GIGA_ZAUR("Giga Zaur"),
    MEKA_DRAGON("Meka Dragon"),
    ALIENOID("Alienoid"),
    CYBER_BUNNY("Cyber Bunny"),
    THE_KING("The King"),
    KRAKEN("Kraken")
}
