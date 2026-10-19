package cli

import dice.DieFace
import player.Player
import player.PlayerController
import player.ShopAction
import shop.PowerCard
import java.util.Scanner

/**
 * Interactive standard input / console controller for human players.
 */
class ConsolePlayerController(
    private val scanner: Scanner = Scanner(System.`in`)
) : PlayerController {

    override fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>
    ): List<Int> {
        println("    Dice to keep/reroll (Current: ${currentDice.mapIndexed { i, f -> "[$i]:$f" }.joinToString(", ")})")
        println("    Enter 0-based indices to KEEP separated by space (or press Enter to reroll all unkept, 'all' to keep all):")
        
        if (!scanner.hasNextLine()) {
            return currentDice.indices.toList()
        }

        val input = scanner.nextLine().trim()
        if (input.equals("all", ignoreCase = true)) {
            return currentDice.indices.toList()
        }
        if (input.isBlank()) {
            return emptyList()
        }

        return input.split(" ")
            .mapNotNull { it.toIntOrNull() }
            .filter { it in currentDice.indices }
    }

    override fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int
    ): Boolean {
        println("    ⚠️ ${player.name}, you took $damage damage from ${attacker.name}! Do you want to yield Tokyo? (y/N):")
        if (!scanner.hasNextLine()) return player.health <= 3
        val input = scanner.nextLine().trim().lowercase()
        return input == "y" || input == "yes"
    }

    override fun chooseShopAction(
        player: Player,
        availableCards: List<PowerCard>,
        sweepCost: Int
    ): ShopAction {
        println("    🛒 Shop (You have ${player.energy} ⚡):")
        availableCards.forEachIndexed { i, card ->
            println("       [$i] ${card.name} (${card.cost} ⚡) - ${card.description} [${card.type}]")
        }
        println("       [s] Sweep shop ($sweepCost ⚡)")
        println("       [p] Pass turn")
        println("    Enter choice (index to buy, 's' to sweep, 'p' to pass):")

        if (!scanner.hasNextLine()) return ShopAction.Pass
        val input = scanner.nextLine().trim().lowercase()

        return when {
            input == "s" || input == "sweep" -> ShopAction.Sweep
            input == "p" || input == "pass" || input.isBlank() -> ShopAction.Pass
            else -> {
                val idx = input.toIntOrNull()
                if (idx != null && idx in availableCards.indices) {
                    ShopAction.Buy(availableCards[idx])
                } else {
                    ShopAction.Pass
                }
            }
        }
    }
}
