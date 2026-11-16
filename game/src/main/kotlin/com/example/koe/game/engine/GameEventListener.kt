package com.example.koe.game.engine

import com.example.koe.game.dice.DieFace
import com.example.koe.game.player.Player
import com.example.koe.game.shop.CardEffectResult
import com.example.koe.game.shop.PowerCard

/**
 * Reasons why a game ended.
 */
sealed interface WinReason {
    data class VictoryPointsReached(val points: Int) : WinReason
    object LastMonsterStanding : WinReason
}

/**
 * Event listener interface for King of Tokyo game events.
 * Enables zero-coupling UI binding for Android (Jetpack Compose / Views),
 * Desktop (Windows / Linux Compose / JavaFX / Swing), CLI, and network broadcasting.
 */
interface GameEventListener {
    fun onGameStarted(players: List<Player>) {}
    fun onTurnStarted(player: Player, roundNumber: Int, isInTokyo: Boolean) {}
    fun onTurnSkipped(player: Player) {}
    fun onDiceRolled(player: Player, rollNumber: Int, maxRolls: Int, dice: List<DieFace>, keptIndices: List<Int>) {}
    /** A player chose the dice to keep after roll [rollNumber]; keeping all of them ends the rolls. */
    fun onDiceKept(player: Player, rollNumber: Int, keptIndices: List<Int>) {}
    fun onDiceResolved(player: Player, summary: DiceResolutionSummary) {}
    fun onMechanceteGained(player: Player, amount: Int, total: Int) {}
    fun onPlayerDamaged(target: Player, attacker: Player, damage: Int, remainingHealth: Int) {}
    fun onPlayerHealed(player: Player, amount: Int, currentHealth: Int) {}
    fun onPlayerEnteredTokyo(player: Player, zone: TokyoZone) {}
    fun onPlayerYieldedTokyo(player: Player, zone: TokyoZone) {}
    fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) {}
    fun onShopSwept(player: Player) {}
    fun onMechanceteCardChosen(player: Player, tier: Int, card: PowerCard, result: CardEffectResult) {}
    fun onTurnEnded(player: Player) {}
    fun onPlayerEliminated(player: Player, reason: String) {}
    fun onGameOver(winner: Player, reason: WinReason) {}
}

/**
 * Composite listener to support multiple subscribers simultaneously.
 */
class CompositeGameEventListener(
    private val listeners: MutableList<GameEventListener> = mutableListOf()
) : GameEventListener {

    fun addListener(listener: GameEventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: GameEventListener) {
        listeners.remove(listener)
    }

    override fun onGameStarted(players: List<Player>) =
        listeners.forEach { it.onGameStarted(players) }

    override fun onTurnStarted(player: Player, roundNumber: Int, isInTokyo: Boolean) =
        listeners.forEach { it.onTurnStarted(player, roundNumber, isInTokyo) }

    override fun onTurnSkipped(player: Player) =
        listeners.forEach { it.onTurnSkipped(player) }

    override fun onDiceRolled(player: Player, rollNumber: Int, maxRolls: Int, dice: List<DieFace>, keptIndices: List<Int>) =
        listeners.forEach { it.onDiceRolled(player, rollNumber, maxRolls, dice, keptIndices) }

    override fun onDiceKept(player: Player, rollNumber: Int, keptIndices: List<Int>) =
        listeners.forEach { it.onDiceKept(player, rollNumber, keptIndices) }

    override fun onDiceResolved(player: Player, summary: DiceResolutionSummary) =
        listeners.forEach { it.onDiceResolved(player, summary) }

    override fun onMechanceteGained(player: Player, amount: Int, total: Int) =
        listeners.forEach { it.onMechanceteGained(player, amount, total) }

    override fun onPlayerDamaged(target: Player, attacker: Player, damage: Int, remainingHealth: Int) =
        listeners.forEach { it.onPlayerDamaged(target, attacker, damage, remainingHealth) }

    override fun onPlayerHealed(player: Player, amount: Int, currentHealth: Int) =
        listeners.forEach { it.onPlayerHealed(player, amount, currentHealth) }

    override fun onPlayerEnteredTokyo(player: Player, zone: TokyoZone) =
        listeners.forEach { it.onPlayerEnteredTokyo(player, zone) }

    override fun onPlayerYieldedTokyo(player: Player, zone: TokyoZone) =
        listeners.forEach { it.onPlayerYieldedTokyo(player, zone) }

    override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) =
        listeners.forEach { it.onCardPurchased(player, card, result, cost) }

    override fun onShopSwept(player: Player) =
        listeners.forEach { it.onShopSwept(player) }

    override fun onMechanceteCardChosen(player: Player, tier: Int, card: PowerCard, result: CardEffectResult) =
        listeners.forEach { it.onMechanceteCardChosen(player, tier, card, result) }

    override fun onTurnEnded(player: Player) =
        listeners.forEach { it.onTurnEnded(player) }

    override fun onPlayerEliminated(player: Player, reason: String) =
        listeners.forEach { it.onPlayerEliminated(player, reason) }

    override fun onGameOver(winner: Player, reason: WinReason) =
        listeners.forEach { it.onGameOver(winner, reason) }
}
