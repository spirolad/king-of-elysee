package game

import player.Player
import shop.PowerCard

/**
 * Configuration options for a King of Tokyo match.
 */
data class GameConfig(
    val baseDiceCount: Int = 6,
    val maxRerolls: Int = 2,
    val winningVictoryPoints: Int = 20,
    val tokyoStartTurnVp: Int = 2,
    val tokyoEnterVp: Int = 1,
    val hasTokyoBay: Boolean = false
)

/**
 * Immutable read-only view of the game state for UI inspection and AI decision making.
 */
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val roundNumber: Int,
    val tokyoOccupants: List<Player>,
    val faceUpCards: List<PowerCard>,
    val isGameOver: Boolean,
    val winner: Player?
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)

    val activePlayers: List<Player>
        get() = players.filter { it.isAlive }
}
