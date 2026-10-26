package com.example.koe.game.player

import com.example.koe.game.dice.DieFace
import com.example.koe.game.shop.PowerCard

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
 */
interface PlayerController {
    /**
     * Called after a dice roll to determine which dice to keep for the next re-roll.
     * @param rollNumber Current roll attempt (1, 2, or 3).
     * @param currentDice Current faces of all active dice.
     * @param currentlyKept Booleans indicating which dice are currently locked/kept.
     * @return List of indices (0 until currentDice.size) that the player wishes to keep.
     */
    fun chooseDiceToKeep(
        player: Player,
        rollNumber: Int,
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
    fun shouldYieldTokyo(
        player: Player,
        attacker: Player,
        damage: Int
    ): Boolean

    /**
     * Called during the shop phase to choose whether to buy a card, sweep, or pass.
     * @param player The player whose turn it is.
     * @param availableCards The 3 face-up cards currently in the shop.
     * @param sweepCost Cost to sweep the shop (typically 2 energy).
     * @return A [ShopAction] chosen by the player.
     */
    fun chooseShopAction(
        player: Player,
        availableCards: List<PowerCard>,
        sweepCost: Int
    ): ShopAction
}
