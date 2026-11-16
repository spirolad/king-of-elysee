package com.example.koe.game.player

import com.example.koe.game.dice.DieFace
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer

/**
 * Action choices a player can make when visiting the shop.
 */
sealed interface ShopAction {
    object Pass : ShopAction
    object Sweep : ShopAction
    data class Buy(val card: PowerCard) : ShopAction
}

/**
 * Interface for decoupling player input / decisions from the game engine.
 * Allows seamless adaptation for Android UI, Desktop GUI (Windows/Linux), CLI, or AI bots.
 * Adheres to Dependency Inversion (DIP) and Interface Segregation (ISP).
 * Decision methods are suspending so that a UI controller can wait for user input.
 */
interface PlayerController {
    /**
     * Called at the start of the roll phase, before the first roll.
     * Returns when the player throws the dice.
     * @param player The player whose turn it is.
     * @param maxRolls Total rolls allowed this turn (first roll included).
     */
    suspend fun awaitFirstRoll(player: Player, maxRolls: Int) {}

    /**
     * Called after a dice roll to determine which dice to keep for the next re-roll.
     * @param rollNumber Current roll attempt (1-based).
     * Return all indices to stop rolling.
     * @param currentDice Current faces of all active dice.
     * @param currentlyKept Booleans indicating which dice are currently locked/kept.
     * @return List of indices (0 until currentDice.size) that the player wishes to keep.
     */
    suspend fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
        maxRolls: Int,
        currentDice: List<DieFace>,
        currentlyKept: List<Boolean>
    ): List<Int>

    /**
     * Called when a player inside Tokyo takes damage and can decide whether to yield Tokyo.
     * @param player The player in Tokyo taking damage.
     * @param attacker The player who dealt the damage.
     * @param damage The amount of damage taken.
     * @return true if the player yields Tokyo, false if they choose to stay.
     */
    suspend fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int
    ): Boolean

    /**
     * Called during the shop phase to choose whether to buy a card, sweep, or pass.
     * @param player The player whose turn it is.
     * @param offers The face-up cards with the price this player would pay.
     * @param sweepCost Cost to sweep the shop (typically 2 energy).
     * @return A [ShopAction] chosen by the player.
     */
    suspend fun chooseShopAction(
        player: Player,
        offers: List<ShopOffer>,
        sweepCost: Int
    ): ShopAction

    /**
     * Called at the end of the turn when [player] reached Méchanceté [tier].
     * @param player The player who reached the tier.
     * @param tier The Méchanceté tier reached.
     * @param choices The cards of that tier the player may take.
     * @return One of [choices].
     */
    suspend fun chooseMechanceteCard(
        player: Player,
        tier: Int,
        choices: List<PowerCard>
    ): PowerCard
}
