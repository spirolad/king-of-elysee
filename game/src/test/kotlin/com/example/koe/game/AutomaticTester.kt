package com.example.koe.game

import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.RandomDiceRoller
import com.example.koe.game.engine.GameConfig
import com.example.koe.game.engine.GameEngine
import com.example.koe.game.engine.GameEventListener
import com.example.koe.game.engine.WinReason
import com.example.koe.game.player.AiPlayerController
import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.PlayerId
import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardEffectResult
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.Shop
import kotlinx.coroutines.runBlocking
import java.util.Random

/**
 * Result data holder for a single simulated test game.
 */
data class GameSimulationResult(
    val gameIndex: Int,
    val seed: Long,
    val playerCount: Int,
    val roundsPlayed: Int,
    val winner: Player,
    val winReason: WinReason?,
    val finalScores: Map<String, Int>,
    val finalHealths: Map<String, Int>,
    val cardsPurchasedTotal: Int,
    val invariantsPassed: Boolean,
    val failureReasons: List<String> = emptyList()
)

/**
 * Aggregate summary report for a batch of simulated test games.
 */
data class BatchSimulationReport(
    val totalGames: Int,
    val successfulGames: Int,
    val failedGames: Int,
    val vpWins: Int,
    val eliminationWins: Int,
    val avgRounds: Double,
    val minRounds: Int,
    val maxRounds: Int,
    val monsterWins: Map<Monster, Int>,
    val gameResults: List<GameSimulationResult>
) {
    fun printReport() {
        println("\n=======================================================")
        println("       AUTOMATED TEST SUITE: 1000 GAMES SUMMARY         ")
        println("=======================================================\n")
        println(" Total Games Simulated : $totalGames")
        println(" Games Invariant-Passed: $successfulGames (" + "%.1f".format(successfulGames * 100.0 / totalGames) + "%)")
        println(" Invariant Violations  : $failedGames")
        println("-------------------------------------------------------")
        println(" Victory Point Wins    : $vpWins (" + "%.1f".format(vpWins * 100.0 / totalGames) + "%)")
        println(" Elimination Wins      : $eliminationWins (" + "%.1f".format(eliminationWins * 100.0 / totalGames) + "%)")
        println(" Average Rounds / Game : " + "%.2f".format(avgRounds) + " (min: $minRounds, max: $maxRounds)")
        println("-------------------------------------------------------")
        println(" Win Distribution by Monster:")
        monsterWins.entries.sortedByDescending { it.value }.forEach { (monster, wins) ->
            val winPct = "%.1f".format(wins * 100.0 / totalGames)
            println("   - ${monster.displayName.padEnd(14)}: $wins wins ($winPct%)")
        }
        println("=======================================================\n")
    }
}

/**
 * Configuration for automated test game generation.
 */
data class GameGeneratorConfig(
    val totalGames: Int = 100,
    val baseSeed: Long = 42L,
    val maxRoundsPerGame: Int = 150
)

/**
 * Automated game generator and invariant verification engine.
 */
class AutomaticTester(
    private val generatorConfig: GameGeneratorConfig = GameGeneratorConfig()
) {

    /**
     * Generates and executes [count] simulated games, validating all system invariants.
     */
    fun runBatch(count: Int = generatorConfig.totalGames): BatchSimulationReport {
        val results = mutableListOf<GameSimulationResult>()

        for (i in 1..count) {
            val seed = generatorConfig.baseSeed + i * 1000L
            val result = executeSingleTestGame(gameIndex = i, seed = seed)
            results.add(result)
        }

        val passedCount = results.count { it.invariantsPassed }
        val failedCount = results.size - passedCount
        val vpWins = results.count { it.winReason is WinReason.VictoryPointsReached }
        val elimWins = results.count { it.winReason is WinReason.LastMonsterStanding }
        val avgRounds = if (results.isNotEmpty()) results.map { it.roundsPlayed }.average() else 0.0
        val minRounds = results.minOfOrNull { it.roundsPlayed } ?: 0
        val maxRounds = results.maxOfOrNull { it.roundsPlayed } ?: 0

        val monsterWins = mutableMapOf<Monster, Int>()
        for (res in results) {
            monsterWins[res.winner.monster] = (monsterWins[res.winner.monster] ?: 0) + 1
        }

        return BatchSimulationReport(
            totalGames = results.size,
            successfulGames = passedCount,
            failedGames = failedCount,
            vpWins = vpWins,
            eliminationWins = elimWins,
            avgRounds = avgRounds,
            minRounds = minRounds,
            maxRounds = maxRounds,
            monsterWins = monsterWins,
            gameResults = results
        )
    }

    /**
     * Executes a single generated test game with diverse parameters and checks invariants.
     */
    fun executeSingleTestGame(gameIndex: Int, seed: Long): GameSimulationResult {
        val rng = Random(seed)

        // Varied player counts: 2 to 6 players
        val playerCount = 2 + (gameIndex % 5) // alternates 2, 3, 4, 5, 6
        val availableMonsters = Monster.entries.shuffled(rng)
        val selectedMonsters = availableMonsters.take(playerCount)

        val players = selectedMonsters.mapIndexed { idx, monster ->
            Player(
                id = PlayerId("p${idx + 1}"),
                name = "${monster.displayName}_P${idx + 1}",
                monster = monster
            )
        }

        // Varied AI strategy parameters (yield threshold between 2 and 5)
        val controllers = players.associate { player ->
            val yieldThreshold = 2 + rng.nextInt(4)
            player.id to AiPlayerController(healthThresholdToYield = yieldThreshold)
        }

        // Varied victory conditions: standard 20 VP, or slight variations (e.g. 15 to 25 VP)
        val targetVp = 18 + (gameIndex % 5) // 18, 19, 20, 21, 22
        val gameConfig = GameConfig(
            winningVictoryPoints = targetVp,
            hasTokyoBay = playerCount >= 5
        )

        val diceRoller: DiceRoller = RandomDiceRoller(Random(seed + 1))
        val shop = Shop(
            initialCards = CardCatalog.createStandardDeck().shuffled(rng),
            shuffleDeck = false
        )

        var recordedWinReason: WinReason? = null
        var totalCardsBought = 0

        val trackingListener = object : GameEventListener {
            override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) {
                totalCardsBought++
            }

            override fun onGameOver(winner: Player, reason: WinReason) {
                recordedWinReason = reason
            }
        }

        val engine = GameEngine(
            players = players,
            controllers = controllers,
            config = gameConfig,
            shop = shop,
            diceRoller = diceRoller,
            eventListener = trackingListener
        )

        engine.start()
        var roundsCounter = 0
        while (!engine.isGameOver && roundsCounter < generatorConfig.maxRoundsPerGame) {
            runBlocking { engine.playTurn() }
            roundsCounter = engine.roundNumber
        }

        // Invariant Validation
        val failureReasons = mutableListOf<String>()

        if (!engine.isGameOver) {
            failureReasons.add("Game did not terminate within maximum round limit (${generatorConfig.maxRoundsPerGame})")
        }

        val winner = engine.winner
        if (winner == null) {
            failureReasons.add("Game finished without a designated winner")
        } else {
            if (!winner.isAlive) {
                failureReasons.add("Winner ${winner.name} is not alive (health=${winner.health})")
            }

            val reason = recordedWinReason
            when (reason) {
                is WinReason.VictoryPointsReached -> {
                    if (winner.victoryPoints < targetVp) {
                        failureReasons.add("Winner VP (${winner.victoryPoints}) is less than required threshold ($targetVp)")
                    }
                }
                is WinReason.LastMonsterStanding -> {
                    val alive = players.filter { it.isAlive }
                    if (alive.size > 1 && !alive.contains(winner)) {
                        failureReasons.add("Winner was declared by elimination but multiple monsters remain alive (${alive.size})")
                    }
                }
                null -> {
                    failureReasons.add("No win reason recorded during game over event")
                }
            }
        }

        // Validate state consistency for all players
        for (p in players) {
            if (p.health < 0) {
                failureReasons.add("Player ${p.name} has negative health (${p.health})")
            }
            if (p.energy < 0) {
                failureReasons.add("Player ${p.name} has negative energy (${p.energy})")
            }
            if (p.victoryPoints < 0) {
                failureReasons.add("Player ${p.name} has negative VP (${p.victoryPoints})")
            }
            if (!p.isAlive && p.health > 0) {
                failureReasons.add("Dead player ${p.name} has positive health (${p.health})")
            }
            if (p.isAlive && p.health == 0) {
                failureReasons.add("Alive player ${p.name} has zero health")
            }
        }

        // Validate Tokyo Board state
        for (occupant in engine.tokyoBoard.occupants) {
            if (!occupant.isAlive) {
                failureReasons.add("Tokyo occupant ${occupant.name} is dead on board")
            }
        }

        return GameSimulationResult(
            gameIndex = gameIndex,
            seed = seed,
            playerCount = playerCount,
            roundsPlayed = engine.roundNumber,
            winner = winner ?: players.first(),
            winReason = recordedWinReason,
            finalScores = players.associate { it.name to it.victoryPoints },
            finalHealths = players.associate { it.name to it.health },
            cardsPurchasedTotal = totalCardsBought,
            invariantsPassed = failureReasons.isEmpty(),
            failureReasons = failureReasons
        )
    }

    companion object {
        /**
         * Convenience method to run 100 test games and throw if any game violates invariants.
         */
        fun run100Games(): BatchSimulationReport {
            val tester = AutomaticTester()
            val report = tester.runBatch(100)
            if (report.failedGames > 0) {
                val failureDetails = report.gameResults
                    .filter { !it.invariantsPassed }
                    .joinToString("\n") { "Game #${it.gameIndex}: ${it.failureReasons.joinToString(", ")}" }
                error("Automatic tester encountered ${report.failedGames} failures:\n$failureDetails")
            }
            return report
        }
    }
}
