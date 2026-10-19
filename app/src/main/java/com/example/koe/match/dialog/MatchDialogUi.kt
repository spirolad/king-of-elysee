package com.example.koe.match.dialog

import androidx.compose.runtime.Immutable
import com.example.koe.match.Candidate

enum class CardKind { POUVOIR, ACTION, MECHANCETE }

@Immutable
data class PowerCardUi(
    val id: String,
    val title: String,
    val description: String,
    val kind: CardKind,
    // null hides the cost pill (méchanceté cards).
    val cost: Int?,
    // Green check in the header.
    val owned: Boolean = false,
    // false = 50 % alpha, not selectable (too expensive).
    val enabled: Boolean = true,
    // Detail dialog only: "Payée 7 Fonds publics au tour 6".
    val paidLine: String? = null,
)

@Immutable
data class RankingRowUi(
    val candidate: Candidate,
    val votes: Int,
)

sealed interface MatchDialog {
    /** Scrim tap and back close it; false for decisions the player must answer. */
    val dismissible: Boolean

    data class Pause(
        val turn: Int,
        val currentPlayer: Candidate,
        val ranking: List<RankingRowUi>,
    ) : MatchDialog {
        override val dismissible = false
    }

    data class QuitConfirm(val king: Candidate) : MatchDialog {
        override val dismissible = true
    }

    data class YieldElysee(
        val king: Candidate,
        val attacker: Candidate,
        val popularityLoss: Int,
        val popularityLeft: Int,
    ) : MatchDialog {
        override val dismissible = false
    }

    data class Mechancete(
        val level: Int,
        val choices: List<PowerCardUi>,
    ) : MatchDialog {
        override val dismissible = false
    }

    data class CardDetail(val card: PowerCardUi) : MatchDialog {
        override val dismissible = true
    }

    data class PlayerDetails(
        val candidate: Candidate,
        val isKing: Boolean,
        val playerNumber: Int,
        val popularite: Int,
        val votes: Int,
        val fonds: Int,
        val mechancete: Int,
        val cards: List<PowerCardUi>,
    ) : MatchDialog {
        override val dismissible = true
    }

    data class Shop(
        val fonds: Int,
        val cards: List<PowerCardUi>,
        val renewCost: Int,
    ) : MatchDialog {
        override val dismissible = true
    }

    data class MyCards(
        val cards: List<PowerCardUi>,
        val mechancete: Int,
        val nextTier: Int,
    ) : MatchDialog {
        override val dismissible = true
    }
}

/** Every dialog button. Defaults are no-ops so tests pass only what they assert. */
class MatchDialogActions(
    // Scrim / back / close / OK / RESTER-CÉDER after their own callback.
    val onDismiss: () -> Unit = {},
    val onResume: () -> Unit = {},
    val onRules: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onQuitRequest: () -> Unit = {},
    val onQuitConfirmed: () -> Unit = {},
    val onStay: () -> Unit = {},
    val onYield: () -> Unit = {},
    val onMechanceteValidate: (PowerCardUi) -> Unit = {},
    val onBuy: (PowerCardUi) -> Unit = {},
    val onRenew: () -> Unit = {},
    // Long press on any card.
    val onCardDetails: (PowerCardUi) -> Unit = {},
)

object MatchDialogTags {
    const val SCRIM = "dialog:scrim"
    const val PAUSE = "dialog:pause"
    const val QUIT = "dialog:quit"
    const val YIELD = "dialog:yield"
    const val MECHANCETE = "dialog:mechancete"
    const val CARD_DETAIL = "dialog:cardDetail"
    const val PLAYER = "dialog:player"
    const val SHOP = "dialog:shop"
    const val MY_CARDS = "dialog:myCards"
    const val CLOSE = "dialog:close"
    // Shop TON CHOIX panel.
    const val CHOICE = "dialog:choice"
    // Player stats inset.
    const val STATS = "dialog:stats"
    const val MECHANCETE_PANEL = "dialog:mechancetePanel"
    fun card(id: String) = "card:$id"
}
