package com.example.koe.match.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.koe.game.dice.DiceRoller
import com.example.koe.game.dice.DieFace
import com.example.koe.game.dice.RandomDiceRoller
import com.example.koe.game.engine.DiceResolutionSummary
import com.example.koe.game.engine.GameConfig
import com.example.koe.game.engine.GameEngine
import com.example.koe.game.engine.GameEventListener
import com.example.koe.game.engine.TokyoZone
import com.example.koe.game.engine.WinReason
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerController
import com.example.koe.game.player.ShopAction
import com.example.koe.game.shop.CardEffectResult
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.Shop
import com.example.koe.match.Candidate
import com.example.koe.match.MatchUiState
import com.example.koe.match.RollUi
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.PowerCardUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Plays one match for the [human] candidate; the AI opponents are paced by [aiDelayMillis]. */
class MatchViewModel(
    private val human: Candidate,
    diceRoller: DiceRoller = RandomDiceRoller(),
    shop: Shop = Shop(),
    aiDelayMillis: Long = AI_DELAY_MILLIS,
) : ViewModel() {

    private val players: List<Player> = (listOf(human) + Candidate.entries.filter { it != human })
        .map { it.toPlayer() }
    private val humanPlayer: Player = players.first()

    private var pending: HumanDecision? = null
    private var shownDice: List<DieFace> = emptyList()
    private var keptDice: List<Int> = emptyList()
    private var keptSelection: Set<Int> = emptySet()
    private var requests: List<DialogRequest> = emptyList()
    private var gameJob: Job? = null
    private val tally = TurnTally()

    private val listener = object : GameEventListener {
        override fun onTurnStarted(player: Player, roundNumber: Int, isInTokyo: Boolean) {
            tally.start(player)
            shownDice = emptyList()
            keptDice = emptyList()
            refresh()
        }

        override fun onDiceRolled(
            player: Player,
            rollNumber: Int,
            maxRolls: Int,
            dice: List<DieFace>,
            keptIndices: List<Int>,
        ) {
            shownDice = dice
            keptDice = keptIndices.toList()
            refresh()
        }

        override fun onDiceKept(player: Player, rollNumber: Int, keptIndices: List<Int>) {
            keptDice = keptIndices.toList()
            refresh()
        }

        override fun onTurnSkipped(player: Player) = refresh()
        override fun onDiceResolved(player: Player, summary: DiceResolutionSummary) {
            tally.resolved(player, summary)
            refresh()
        }
        override fun onMechanceteGained(player: Player, amount: Int, total: Int) = refresh()
        override fun onPlayerDamaged(target: Player, attacker: Player, damage: Int, remainingHealth: Int) {
            tally.hit(attacker, target, damage)
            refresh()
        }
        override fun onPlayerHealed(player: Player, amount: Int, currentHealth: Int) = refresh()
        override fun onPlayerEnteredTokyo(player: Player, zone: TokyoZone) = refresh()
        override fun onPlayerYieldedTokyo(player: Player, zone: TokyoZone) = refresh()
        override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) = refresh()
        override fun onShopSwept(player: Player) = refresh()
        override fun onMechanceteCardChosen(player: Player, tier: Int, card: PowerCard, result: CardEffectResult) =
            refresh()
        override fun onTurnEnded(player: Player) = refresh()
        override fun onPlayerEliminated(player: Player, reason: String) = refresh()
        override fun onGameOver(winner: Player, reason: WinReason) = refresh()
    }

    private val engine = GameEngine(
        players = players,
        controllers = players.associate { player ->
            val controller: PlayerController = if (player.id == humanPlayer.id) {
                HumanPlayerController { decision -> onHumanDecision(decision) }
            } else {
                PacedAiController(
                    thinkMillis = aiDelayMillis,
                    resultMillis = aiDelayMillis * AI_RESULT_FACTOR,
                )
            }
            player.id to controller
        },
        config = GameConfig(),
        diceRoller = diceRoller,
        shop = shop,
        eventListener = listener,
    )

    private val _uiState = MutableStateFlow(
        boardState(engine, humanPlayer.id, shownDice, keptDice, canEndTurn = false),
    )

    /** The board as drawn: every value comes from the engine on each refresh. */
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _dialogs = MutableStateFlow<List<MatchDialog>>(emptyList())

    /** Open dialogs; the last element is the visible one. */
    val dialogs: StateFlow<List<MatchDialog>> = _dialogs.asStateFlow()

    /** Starts the match once; later calls do nothing. */
    fun start() {
        if (gameJob != null) return
        gameJob = viewModelScope.launch { engine.runGame() }
    }

    // Board

    fun onPauseClick() = push(DialogRequest.Pause)

    fun onOpponentClick(candidate: Candidate) = push(DialogRequest.PlayerDetails(candidate))

    fun onShopClick() = push(DialogRequest.Shop)

    fun onCardsClick() = push(DialogRequest.MyCards)

    fun onEndTurnClick() {
        val decision = pending as? HumanDecision.Shop ?: return
        decision.answer.complete(ShopAction.Pass)
        pending = null
        requests = requests.filterNot { it == DialogRequest.Shop }
        refresh()
    }

    /** Toggles a die's keep state while the human is choosing which dice to keep. */
    fun onDieClick(index: Int) {
        if (pending !is HumanDecision.KeepDice || index !in shownDice.indices) return
        keptSelection = if (index in keptSelection) keptSelection - index else keptSelection + index
        refresh()
    }

    /** Throws the dice, or rerolls the dice not kept. */
    fun onRollClick() {
        when (val decision = pending) {
            is HumanDecision.Roll -> decision.answer.complete(Unit)
            is HumanDecision.KeepDice -> decision.answer.complete(keptSelection.sorted())
            else -> return
        }
        pending = null
        refresh()
    }

    /** Keeps every die, which ends the rolls. */
    fun onValidateDiceClick() {
        val decision = pending as? HumanDecision.KeepDice ?: return
        decision.answer.complete(shownDice.indices.toList())
        pending = null
        refresh()
    }

    // Dialogs

    fun onDismiss() {
        // Decisions stay open until they are answered.
        if (_dialogs.value.lastOrNull()?.dismissible == true) pop()
    }

    fun onResume() = pop()

    // The pause dialog is replaced, not stacked, so the confirmation is the only one left.
    fun onQuitRequest() = replaceTop(DialogRequest.Quit)

    fun onQuitConfirmed() {
        gameJob?.cancel()
        requests = emptyList()
        refresh()
    }

    fun onStay() {
        val decision = pending as? HumanDecision.Yield ?: return
        decision.answer.complete(false)
        pending = null
        pop()
    }

    fun onYield() {
        val decision = pending as? HumanDecision.Yield ?: return
        decision.answer.complete(true)
        pending = null
        pop()
    }

    fun onMechanceteValidate(card: PowerCardUi) {
        val decision = pending as? HumanDecision.Mechancete ?: return
        val choice = decision.choices.firstOrNull { it.id.value == card.id } ?: return
        decision.answer.complete(choice)
        pending = null
        pop()
    }

    fun onBuy(card: PowerCardUi) {
        val decision = pending as? HumanDecision.Shop ?: return
        val offer = decision.offers.firstOrNull { it.card.id.value == card.id } ?: return
        decision.answer.complete(ShopAction.Buy(offer.card))
        pending = null
        pop()
    }

    fun onRenew() {
        val decision = pending as? HumanDecision.Shop ?: return
        decision.answer.complete(ShopAction.Sweep)
        pending = null
        // The shop stays open; the engine asks again with the new offers.
        refresh()
    }

    fun onCardDetails(card: PowerCardUi) = push(DialogRequest.CardDetail(card))

    private fun onHumanDecision(decision: HumanDecision?) {
        pending = decision
        when (decision) {
            is HumanDecision.KeepDice -> keptSelection = decision.currentlyKept.withIndex()
                .filter { it.value }
                .map { it.index }
                .toSet()
            is HumanDecision.Yield -> requests = requests + DialogRequest.Yield
            is HumanDecision.Mechancete -> requests = requests + DialogRequest.Mechancete
            else -> Unit
        }
        refresh()
    }

    private fun push(request: DialogRequest) {
        requests = requests + request
        refresh()
    }

    private fun pop() {
        requests = requests.dropLast(1)
        refresh()
    }

    private fun replaceTop(request: DialogRequest) {
        requests = requests.dropLast(1) + request
        refresh()
    }

    private fun refresh() {
        val decision = pending
        requests = requests.filter { request ->
            when (request) {
                DialogRequest.Yield -> decision is HumanDecision.Yield
                DialogRequest.Mechancete -> decision is HumanDecision.Mechancete
                else -> true
            }
        }
        val roll = when (decision) {
            is HumanDecision.Roll -> RollUi(0, decision.maxRolls)
            is HumanDecision.KeepDice -> RollUi(decision.rollNumber, decision.maxRolls)
            else -> null
        }
        _uiState.value = boardState(
            engine,
            humanPlayer.id,
            shownDice,
            if (decision is HumanDecision.KeepDice) keptSelection.toList() else keptDice,
            canEndTurn = decision is HumanDecision.Shop,
            roll = roll,
            turnResult = tally.result,
            votesDelta = tally.votesDelta,
            tweetBanner = tally.tweetBanner(humanPlayer.id),
        )
        _dialogs.value = requests.map { buildDialog(it, decision) }
    }

    // Requests for Yield and Mechancete are filtered in refresh, so their decision is always present.
    private fun buildDialog(request: DialogRequest, decision: HumanDecision?): MatchDialog = when (request) {
        DialogRequest.Pause -> pauseDialog(engine)
        DialogRequest.Quit -> MatchDialog.QuitConfirm(_uiState.value.king)
        is DialogRequest.PlayerDetails -> playerDetailsDialog(engine, request.candidate)
        DialogRequest.Shop -> {
            val shop = decision as? HumanDecision.Shop
            if (shop != null) {
                shopDialog(humanPlayer, shop.offers, shop.sweepCost, canBuy = true)
            } else {
                shopDialog(
                    humanPlayer,
                    engine.shop.offers(humanPlayer, engine.alivePlayers),
                    engine.shop.sweepCost,
                    canBuy = false,
                )
            }
        }
        DialogRequest.MyCards -> myCardsDialog(humanPlayer)
        is DialogRequest.CardDetail -> MatchDialog.CardDetail(request.card)
        DialogRequest.Yield -> {
            val yieldDecision = decision as HumanDecision.Yield
            yieldDialog(humanPlayer, yieldDecision.attacker, yieldDecision.damage)
        }
        DialogRequest.Mechancete -> {
            val mechancete = decision as HumanDecision.Mechancete
            mechanceteDialog(mechancete.tier, mechancete.choices)
        }
    }

    companion object {
        const val AI_DELAY_MILLIS = 700L

        // The turn result stays up longer than a single decision.
        private const val AI_RESULT_FACTOR = 2

        fun factory(human: Candidate): ViewModelProvider.Factory = viewModelFactory {
            initializer { MatchViewModel(human) }
        }
    }
}

/** A dialog the player has opened or the engine has asked for; rebuilt from the engine on every refresh. */
private sealed interface DialogRequest {
    data object Pause : DialogRequest
    data object Quit : DialogRequest
    data class PlayerDetails(val candidate: Candidate) : DialogRequest
    data object Shop : DialogRequest
    data object MyCards : DialogRequest
    data class CardDetail(val card: PowerCardUi) : DialogRequest
    data object Yield : DialogRequest
    data object Mechancete : DialogRequest
}
