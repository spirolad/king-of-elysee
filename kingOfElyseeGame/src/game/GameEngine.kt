package game

import dice.DiceRoller
import dice.DieFace
import dice.RandomDiceRoller
import player.Player
import player.PlayerController
import player.PlayerId
import player.ShopAction
import shop.Shop

/**
 * Core game engine orchestrating the rules and flow of King of Tokyo.
 * Adheres to Dependency Inversion (DIP) and Single Responsibility (SRP).
 */
class GameEngine(
    val players: List<Player>,
    private val controllers: Map<PlayerId, PlayerController>,
    val config: GameConfig = GameConfig(hasTokyoBay = players.size >= 5),
    val shop: Shop = Shop(),
    private val diceRoller: DiceRoller = RandomDiceRoller(),
    val eventListener: GameEventListener = CompositeGameEventListener()
) {
    init {
        require(players.size >= 2) { "King of Tokyo requires at least 2 players" }
    }

    val tokyoBoard = TokyoBoard(hasTokyoBay = config.hasTokyoBay)
    var currentPlayerIndex: Int = 0
        private set
    var roundNumber: Int = 1
        private set
    var isGameOver: Boolean = false
        private set
    var winner: Player? = null
        private set

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    val alivePlayers: List<Player>
        get() = players.filter { it.isAlive }

    val state: GameState
        get() = GameState(
            players = players,
            currentPlayerIndex = currentPlayerIndex,
            roundNumber = roundNumber,
            tokyoOccupants = tokyoBoard.occupants,
            faceUpCards = shop.faceUpCards,
            isGameOver = isGameOver,
            winner = winner
        )

    fun start() {
        eventListener.onGameStarted(players)
    }

    /**
     * Executes a single turn for the current player.
     * Returns true if turn completed and game continues, or false if game is over.
     */
    fun playTurn(): Boolean {
        if (isGameOver) return false

        val active = currentPlayer
        if (!active.isAlive) {
            advanceTurn()
            return !isGameOver
        }

        if (active.skipNextTurn) {
            active.skipNextTurn = false
            advanceTurn()
            return !isGameOver
        }

        if (active.hasCard("immunite_parlementaire")) {
            active.hasActionImmunity = true
        }

        if (active.hasCard("infatigable")) {
            active.addEnergy(1)
        }
        if (active.hasCard("eternel")) {
            active.heal(1)
        }

        val isInTokyoAtStart = tokyoBoard.isInsideTokyo(active)
        if (isInTokyoAtStart) {
            active.addVictoryPoints(config.tokyoStartTurnVp)
            checkWinConditions(active)
            if (isGameOver) return false
        }

        eventListener.onTurnStarted(active, roundNumber, isInTokyoAtStart)

        // 1. Roll Dice Phase
        val finalDice = executeDiceRollPhase(active)

        // 2. Resolve Dice Phase
        executeResolveDicePhase(active, finalDice)
        if (isGameOver) return false

        // 3. Shop Phase
        executeShopPhase(active)
        if (isGameOver) return false

        // 4. End Turn Effects
        executeEndTurnPhase(active)

        // 5. Advance Turn
        advanceTurn()
        return !isGameOver
    }

    /**
     * Runs the entire game loop until a winner emerges.
     */
    fun runGame(): Player {
        start()
        while (!isGameOver) {
            playTurn()
        }
        return winner!!
    }

    private fun executeDiceRollPhase(player: Player): List<DieFace> {
        val controller = controllers[player.id] ?: error("No controller registered for player ${player.name}")
        val diceCount = config.baseDiceCount + player.extraDiceCount
        var currentDice = diceRoller.roll(diceCount)
        var keptIndices = mutableListOf<Int>()

        var extraRerolls = 0
        if (player.hasCard("phrase_culte")) extraRerolls++
        if (player.hasCard("sournois")) extraRerolls++
        var rollsLeft = config.maxRerolls + extraRerolls
        var rollNumber = 1

        eventListener.onDiceRolled(player, rollNumber, currentDice, keptIndices)

        while (rollsLeft > 0) {
            val keptChoices = controller.chooseDiceToKeep(
                player = player,
                rollNumber = rollNumber,
                currentDice = currentDice,
                currentlyKept = List(currentDice.size) { it in keptIndices }
            ).distinct().filter { it in currentDice.indices }

            keptIndices = keptChoices.toMutableList()

            // If player keeps all dice, stop rerolling
            if (keptIndices.size == currentDice.size) {
                break
            }

            // Reroll non-kept dice
            val nextDice = currentDice.toMutableList()
            for (i in currentDice.indices) {
                if (i !in keptIndices) {
                    nextDice[i] = diceRoller.rollSingle()
                }
            }
            currentDice = nextDice
            rollNumber++
            rollsLeft--

            eventListener.onDiceRolled(player, rollNumber, currentDice, keptIndices)
        }

        return currentDice
    }

    private fun executeResolveDicePhase(player: Player, dice: List<DieFace>) {
        val isInTokyo = tokyoBoard.isInsideTokyo(player)
        val summary = DiceResolver.resolve(dice, isInTokyo)

        var finalVp = summary.victoryPoints
        var finalEnergy = summary.energy
        var finalAttack = summary.attackDamage
        var finalHeal = summary.actualHeal

        val ones = dice.count { it == DieFace.ONE }
        val twos = dice.count { it == DieFace.TWO }

        if (player.hasCard("populisme")) {
            finalVp += (ones + twos)
        }
        if (player.hasCard("furtif") && ones >= 3) {
            finalVp += 1
        }
        if (player.hasCard("summer_camp_politique")) {
            finalEnergy += ones
        }
        if (player.hasCard("rayon_stellaire")) {
            val lightning = dice.count { it == DieFace.ENERGY }
            finalEnergy += lightning
            if (!isInTokyo) {
                val hearts = dice.count { it == DieFace.HEAL }
                finalHeal += hearts
            }
        }
        if (player.hasCard("privatisation") && isInTokyo && finalVp > 0) {
            finalVp += 1
        }
        if (!isInTokyo && player.hasCard("campagne")) {
            if (finalAttack > 0) finalAttack += 1
        }

        // Apply Points & Energy
        player.addVictoryPoints(finalVp)
        if (summary.wickednessPoints > 0) {
            player.addWickednessPoints(summary.wickednessPoints)
        }
        if (!player.noEnergyNextTurn && finalEnergy > 0) {
            player.addEnergy(finalEnergy)
            alivePlayers.filter { it.id != player.id && it.hasCard("detournement") }.forEach {
                it.addEnergy(1)
            }
        }

        // Apply Heal
        if (finalHeal > 0) {
            val healed = player.heal(finalHeal)
            eventListener.onPlayerHealed(player, healed, player.health)
        }

        eventListener.onDiceResolved(player, summary)
        checkWinConditions(player)
        if (isGameOver) return

        // 49.3 power card check
        if (!isInTokyo && player.hasCard("49_3") && player.energy >= 3 && tokyoBoard.occupants.isNotEmpty() && finalAttack == 0) {
            player.spendEnergy(3)
            val currentOccupant = tokyoBoard.occupants.firstOrNull()
            if (currentOccupant != null) {
                val zone = tokyoBoard.leave(currentOccupant)
                if (zone != null) {
                    eventListener.onPlayerYieldedTokyo(currentOccupant, zone)
                    if (currentOccupant.hasCard("campagne_d_invectives")) {
                        player.damage(2)
                    }
                }
            }
            if (player.isAlive && tokyoBoard.hasEmptySlot) {
                val enteredZone = tokyoBoard.enter(player)
                if (enteredZone != null) {
                    val enterVp = config.tokyoEnterVp + (if (player.hasCard("vu_a_la_tv")) 1 else 0)
                    player.addVictoryPoints(enterVp)
                    eventListener.onPlayerEnteredTokyo(player, enteredZone)
                    checkWinConditions(player)
                }
            }
        }

        // Apply Attack Damage
        if (finalAttack > 0) {
            val targets = tokyoBoard.getTargetsForAttack(player, alivePlayers)
            var tokyoOccupantYielded = false

            for (target in targets) {
                val dmg = target.damage(finalAttack)
                eventListener.onPlayerDamaged(target, player, dmg, target.health)

                if (player.hasCard("scandale_d_etat") && dmg > 0) {
                    target.spendEnergy(1)
                }

                if (!target.isAlive) {
                    eventListener.onPlayerEliminated(target, "Defeated by ${player.name}")
                } else if (tokyoBoard.isInsideTokyo(target) && !isInTokyo) {
                    val targetController = controllers[target.id]
                    if (targetController != null && targetController.shouldYieldTokyo(target, player, dmg)) {
                        val zone = tokyoBoard.leave(target)
                        if (zone != null) {
                            tokyoOccupantYielded = true
                            eventListener.onPlayerYieldedTokyo(target, zone)
                            if (target.hasCard("campagne_d_invectives")) {
                                player.damage(2)
                            }
                        }
                    }
                }
            }

            tokyoBoard.cleanupDeadOccupants()
            checkWinConditions(player)
            if (isGameOver) return

            // If Tokyo has empty space and attacker is outside Tokyo, enter Tokyo
            if (!isInTokyo && tokyoBoard.hasEmptySlot && player.isAlive) {
                val enteredZone = tokyoBoard.enter(player)
                if (enteredZone != null) {
                    val enterVp = config.tokyoEnterVp + (if (player.hasCard("vu_a_la_tv")) 1 else 0)
                    player.addVictoryPoints(enterVp)
                    eventListener.onPlayerEnteredTokyo(player, enteredZone)
                    checkWinConditions(player)
                }
            }
        } else if (!isInTokyo && tokyoBoard.occupants.isEmpty() && player.isAlive) {
            // If Tokyo is completely empty, first monster enters
            val enteredZone = tokyoBoard.enter(player)
            if (enteredZone != null) {
                val enterVp = config.tokyoEnterVp + (if (player.hasCard("vu_a_la_tv")) 1 else 0)
                player.addVictoryPoints(enterVp)
                eventListener.onPlayerEnteredTokyo(player, enteredZone)
                checkWinConditions(player)
            }
        }
    }

    private fun executeEndTurnPhase(player: Player) {
        if (!player.isAlive || isGameOver) return

        if (player.hasCard("affichage") && player.energy >= 3) {
            player.addVictoryPoints(1)
            checkWinConditions(player)
        }
        if (tokyoBoard.isInsideTokyo(player) && player.hasCard("monopole_mediatique")) {
            player.addEnergy(1)
        }

        player.noEnergyNextTurn = false
    }

    private fun executeShopPhase(player: Player) {
        val controller = controllers[player.id] ?: return
        var keepShopping = true

        while (keepShopping && player.isAlive && !isGameOver) {
            when (val action = controller.chooseShopAction(player, shop.faceUpCards, shop.sweepCost)) {
                is ShopAction.Pass -> keepShopping = false
                is ShopAction.Sweep -> {
                    val success = shop.sweep(player)
                    if (success) {
                        eventListener.onShopSwept(player)
                    } else {
                        keepShopping = false
                    }
                }
                is ShopAction.Buy -> {
                    val result = shop.buyCard(player, action.card, alivePlayers)
                    if (result != null) {
                        eventListener.onCardPurchased(player, action.card, result)
                        checkWinConditions(player)
                    } else {
                        keepShopping = false
                    }
                }
            }
        }
    }

    private fun checkWinConditions(triggerPlayer: Player? = null) {
        if (isGameOver) return

        // Win by Victory Points
        val vpWinner = players.firstOrNull { it.isAlive && it.victoryPoints >= config.winningVictoryPoints }
        if (vpWinner != null) {
            isGameOver = true
            winner = vpWinner
            eventListener.onGameOver(vpWinner, WinReason.VictoryPointsReached(vpWinner.victoryPoints))
            return
        }

        // Win by Last Monster Standing
        val survivors = alivePlayers
        if (survivors.size == 1) {
            val soleSurvivor = survivors.first()
            isGameOver = true
            winner = soleSurvivor
            eventListener.onGameOver(soleSurvivor, WinReason.LastMonsterStanding)
            return
        }

        if (survivors.isEmpty()) {
            // Draw or last trigger winner
            isGameOver = true
            winner = triggerPlayer ?: players.first()
            eventListener.onGameOver(winner!!, WinReason.LastMonsterStanding)
        }
    }

    private fun advanceTurn() {
        if (isGameOver) return
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
            if (currentPlayerIndex == 0) {
                roundNumber++
            }
        } while (!currentPlayer.isAlive && alivePlayers.isNotEmpty())
    }
}
