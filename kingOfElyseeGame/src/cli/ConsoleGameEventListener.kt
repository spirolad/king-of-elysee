package cli

import dice.DieFace
import game.DiceResolutionSummary
import game.GameEventListener
import game.TokyoZone
import game.WinReason
import player.Player
import shop.CardEffectResult
import shop.PowerCard

/**
 * Text-based console renderer implementing [GameEventListener].
 * Serves as an exemplar implementation for cross-platform adaptation (Android / Desktop / CLI).
 */
class ConsoleGameEventListener : GameEventListener {

    override fun onGameStarted(players: List<Player>) {
        println("=".repeat(60))
        println("           🎲 WELCOME TO KING OF TOKYO 🎲")
        println("=".repeat(60))
        println("Monsters participating:")
        players.forEach { p ->
            println(" • ${p.name} as ${p.monster.displayName}")
        }
        println("-".repeat(60))
    }

    override fun onTurnStarted(player: Player, roundNumber: Int, isInTokyo: Boolean) {
        println("\n>>> [Round $roundNumber] Turn of ${player.name} (${player.monster.displayName}) <<<")
        println("    Stats: ❤️ HP=${player.health}/${player.maxHealth} | ⚡ Energy=${player.energy} | 🏆 VP=${player.victoryPoints} | 😈 Wickedness=${player.wickednessPoints}")
        if (isInTokyo) {
            println("    📍 Position: INSIDE TOKYO (+2 VP awarded for holding Tokyo!)")
        } else {
            println("    📍 Position: Outside Tokyo")
        }
    }

    override fun onDiceRolled(player: Player, rollNumber: Int, dice: List<DieFace>, keptIndices: List<Int>) {
        val diceDisplay = dice.mapIndexed { idx, face ->
            val keptMarker = if (idx in keptIndices) "[*]" else "   "
            "$face $keptMarker"
        }.joinToString(" | ")
        println("    Roll #$rollNumber: $diceDisplay")
    }

    override fun onDiceResolved(player: Player, summary: DiceResolutionSummary) {
        println("    --- Dice Results Resolved ---")
        if (summary.victoryPoints > 0) println("    +${summary.victoryPoints} 🏆 Victory Points")
        if (summary.wickednessPoints > 0) println("    +${summary.wickednessPoints} 😈 Wickedness Points")
        if (summary.energy > 0) println("    +${summary.energy} ⚡ Energy")
        if (summary.attackDamage > 0) println("    ⚔️ ${summary.attackDamage} Attack Damage dealt")
        if (summary.actualHeal > 0) println("    +${summary.actualHeal} ❤️ Health healed")
        else if (summary.heartsRolled > 0) println("    (Hearts rolled but cannot heal while in Tokyo)")
    }

    override fun onPlayerDamaged(target: Player, attacker: Player, damage: Int, remainingHealth: Int) {
        println("    💥 ${target.name} (${target.monster.displayName}) took $damage damage! (❤️ $remainingHealth HP left)")
    }

    override fun onPlayerHealed(player: Player, amount: Int, currentHealth: Int) {
        println("    💖 ${player.name} healed $amount HP (❤️ $currentHealth/${player.maxHealth})")
    }

    override fun onPlayerEnteredTokyo(player: Player, zone: TokyoZone) {
        println("    👑 ${player.name} boldly stepped into ${zone.displayName}! (+1 VP)")
    }

    override fun onPlayerYieldedTokyo(player: Player, zone: TokyoZone) {
        println("    🏳️ ${player.name} yielded ${zone.displayName} due to damage!")
    }

    override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult) {
        println("    🛍️ ${player.name} bought power card '${card.name}' for ${card.cost} ⚡!")
        println("       Effect: ${result.message}")
    }

    override fun onShopSwept(player: Player) {
        println("    🔄 ${player.name} spent 2 ⚡ to sweep the shop cards!")
    }

    override fun onPlayerEliminated(player: Player, reason: String) {
        println("    💀 ELIMINATED: ${player.name} (${player.monster.displayName}) was knocked out! ($reason)")
    }

    override fun onGameOver(winner: Player, reason: WinReason) {
        println("\n" + "=".repeat(60))
        println("           🏆🏆 GAME OVER - VICTORY! 🏆🏆")
        println("=".repeat(60))
        println("Winner: ${winner.name} (${winner.monster.displayName})")
        when (reason) {
            is WinReason.VictoryPointsReached -> println("Victory Condition: Reached ${reason.points} Victory Points!")
            is WinReason.LastMonsterStanding -> println("Victory Condition: Last monster left standing!")
        }
        println("Final Stats: ❤️ ${winner.health} HP | ⚡ ${winner.energy} Energy | 🏆 ${winner.victoryPoints} VP | 😈 ${winner.wickednessPoints} Wickedness")
        println("=".repeat(60))
    }
}
