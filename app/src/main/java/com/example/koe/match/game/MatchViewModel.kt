package com.example.koe.match.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
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
import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardEffectResult
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.Shop
import com.example.koe.match.Candidate
import com.example.koe.match.MatchUiState
import com.example.koe.match.RollUi
import com.example.koe.match.ShopAvailability
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.PowerCardUi
import kotlin.random.Random
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Plays one match for the [human] candidate; the AI opponents are paced by [aiDelayMillis].
 *
 * The match is reproducible: the engine is seeded from [KEY_SEED] and the human's answers are logged under
 * [KEY_ANSWERS]. A restored match replays those answers before it waits for the human again.
 */
class MatchViewModel(
    private val human: Candidate,
    private val savedState: SavedStateHandle = SavedStateHandle(),
    diceRoller: DiceRoller? = null,
    shop: Shop? = null,
    aiDelayMillis: Long = AI_DELAY_MILLIS,
) : ViewModel() {

    private val seed: Long = savedState.get<Long>(KEY_SEED) ?: Random.nextLong().also { savedState[KEY_SEED] = it }
    private val answers: MutableList<String> =
        savedState.get<ArrayList<String>>(KEY_ANSWERS)?.toMutableList() ?: mutableListOf()
    private val replay = ArrayDeque(answers)

    private val players: List<Player> = (listOf(human) + Candidate.entries.filter { it != human })
        .map { it.toPlayer() }
    private val humanPlayer: Player = players.first()

    private var pending: HumanDecision? = null
    private var shownDice: List<DieFace> = emptyList()
    private var keptDice: List<Int> = emptyList()
    private var keptSelection: Set<Int> = emptySet()
    private var requests: List<DialogRequest> = emptyList()
    private var gameJob: Job? = null
    private var outcome: Outcome? = null
    private val tally = TurnTally()
    private val history = GameHistory()
    private var elyseeBanner = false

    private val listener: GameEventListener = object : GameEventListener {
        override fun onTurnStarted(player: Player, roundNumber: Int, isInTokyo: Boolean) {
            tally.start(player)
            history.turnStarted(player, roundNumber)
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

        override fun onTurnSkipped(player: Player) {
            history.turnSkipped(player, engine.roundNumber)
            refresh()
        }
        override fun onDiceResolved(player: Player, summary: DiceResolutionSummary) {
            tally.resolved(player, summary)
            tally.result?.let {
                history.diceResult(player, it.votes, it.fonds, it.popularite, it.tweets, engine.roundNumber)
            }
            refresh()
        }
        override fun onMechanceteGained(player: Player, amount: Int, total: Int) = refresh()
        override fun onPlayerDamaged(target: Player, attacker: Player, damage: Int, remainingHealth: Int) {
            tally.hit(attacker, target, damage)
            history.tweet(attacker, target, damage, engine.roundNumber)
            refresh()
        }
        override fun onPlayerHealed(player: Player, amount: Int, currentHealth: Int) = refresh()
        override fun onPlayerEnteredTokyo(player: Player, zone: TokyoZone) {
            // A replayed match has already shown the banner, so it stays hidden while the log replays.
            if (player.id == humanPlayer.id && replay.isEmpty()) elyseeBanner = true
            history.enteredElysee(player, engine.roundNumber)
            refresh()
        }
        override fun onPlayerYieldedTokyo(player: Player, zone: TokyoZone) {
            if (player.id == humanPlayer.id) elyseeBanner = false
            history.yieldedElysee(player, engine.roundNumber)
            refresh()
        }
        override fun onCardPurchased(player: Player, card: PowerCard, result: CardEffectResult, cost: Int) {
            history.boughtCard(player, card.name, cost, engine.roundNumber)
            refresh()
        }
        override fun onShopSwept(player: Player) = refresh()
        override fun onMechanceteCardChosen(player: Player, tier: Int, card: PowerCard, result: CardEffectResult) {
            history.mechanceteCard(player, card.name, tier, engine.roundNumber)
            refresh()
        }
        override fun onTurnEnded(player: Player) {
            if (player.id == humanPlayer.id) elyseeBanner = false
            refresh()
        }
        override fun onPlayerEliminated(player: Player, reason: String) {
            history.eliminated(player, engine.roundNumber)
            refresh()
        }
        override fun onGameOver(winner: Player, reason: WinReason) {
            endMatch(Outcome(winner, reason))
            refresh()
        }
    }

    private val engine: GameEngine = GameEngine(
        players = players,
        controllers = players.associate { player ->
            val controller: PlayerController = if (player.id == humanPlayer.id) {
                HumanPlayerController { decision -> onHumanDecision(decision) }
            } else {
                PacedAiController(
                    thinkMillis = aiDelayMillis,
                    resultMillis = aiDelayMillis * AI_RESULT_FACTOR,
                    skipDelays = { replay.isNotEmpty() },
                )
            }
            player.id to controller
        },
        config = GameConfig(),
        diceRoller = diceRoller ?: RandomDiceRoller(java.util.Random(seed)),
        shop = shop ?: Shop(initialCards = CardCatalog.createStandardDeck().shuffled(Random(seed))),
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

    fun onHistoryClick() = push(DialogRequest.History)

    fun onEndTurnClick() {
        val decision = pending as? HumanDecision.Shop ?: return
        requests = requests.filterNot { it == DialogRequest.Shop }
        complete(decision.answer, ShopAction.Pass, "pass")
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
            is HumanDecision.Roll -> complete(decision.answer, Unit, "roll")
            is HumanDecision.KeepDice -> keepDice(decision, keptSelection.sorted())
            else -> return
        }
        refresh()
    }

    /** Keeps every die, which ends the rolls. */
    fun onValidateDiceClick() {
        val decision = pending as? HumanDecision.KeepDice ?: return
        keepDice(decision, shownDice.indices.toList())
        refresh()
    }

    fun onElyseeBannerDismiss() {
        elyseeBanner = false
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
        dropTopRequest()
        complete(decision.answer, false, "yield:false")
        refresh()
    }

    fun onYield() {
        val decision = pending as? HumanDecision.Yield ?: return
        dropTopRequest()
        complete(decision.answer, true, "yield:true")
        refresh()
    }

    fun onMechanceteValidate(card: PowerCardUi) {
        val decision = pending as? HumanDecision.Mechancete ?: return
        val choice = decision.choices.firstOrNull { it.id.value == card.id } ?: return
        dropTopRequest()
        complete(decision.answer, choice, "mechancete:${choice.id.value}")
        refresh()
    }

    fun onBuy(card: PowerCardUi) {
        val decision = pending as? HumanDecision.Shop ?: return
        val offer = decision.offers.firstOrNull { it.card.id.value == card.id } ?: return
        dropTopRequest()
        complete(decision.answer, ShopAction.Buy(offer.card), "buy:${offer.card.id.value}")
        refresh()
    }

    fun onRenew() {
        val decision = pending as? HumanDecision.Shop ?: return
        // The shop stays open; the engine asks again with the new offers.
        complete(decision.answer, ShopAction.Sweep, "sweep")
        refresh()
    }

    fun onCardDetails(card: PowerCardUi) = push(DialogRequest.CardDetail(card))

    private fun onHumanDecision(decision: HumanDecision?) {
        // The engine may ask once more before its job stops; the board takes no input after the game over.
        if (outcome != null && decision != null) return
        if (decision != null && replay.isNotEmpty()) {
            if (replayAnswer(decision, replay.removeFirst())) {
                refresh()
                return
            }
            // The log does not fit the match any more, so the rest of the match is played live.
            replay.clear()
        }
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

    /**
     * Completes the human's [deferred] and logs the encoded answer so a restored match can replay it.
     *
     * Completing resumes the engine at once on Main.immediate, and it may ask the next decision before this call
     * returns, so every state change comes first and [deferred] is completed last.
     */
    private fun <T> complete(deferred: CompletableDeferred<T>, value: T, entry: String) {
        pending = null
        answers += entry
        savedState[KEY_ANSWERS] = ArrayList(answers)
        deferred.complete(value)
    }

    private fun keepDice(decision: HumanDecision.KeepDice, kept: List<Int>) {
        complete(decision.answer, kept, "keep:" + kept.joinToString(","))
    }

    /** Completes [decision] with a logged [entry] without logging it again; false when the entry does not fit it. */
    private fun replayAnswer(decision: HumanDecision, entry: String): Boolean {
        val value = entry.substringAfter(':', "")
        when (decision) {
            is HumanDecision.Roll -> {
                if (entry != "roll") return false
                decision.answer.complete(Unit)
            }
            is HumanDecision.KeepDice -> {
                if (!entry.startsWith("keep:")) return false
                decision.answer.complete(if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() })
            }
            is HumanDecision.Shop -> {
                val action = when {
                    entry == "pass" -> ShopAction.Pass
                    entry == "sweep" -> ShopAction.Sweep
                    entry.startsWith("buy:") -> {
                        val card = decision.offers.firstOrNull { it.card.id.value == value }?.card ?: return false
                        ShopAction.Buy(card)
                    }
                    else -> return false
                }
                decision.answer.complete(action)
            }
            is HumanDecision.Yield -> {
                if (entry != "yield:true" && entry != "yield:false") return false
                decision.answer.complete(entry == "yield:true")
            }
            is HumanDecision.Mechancete -> {
                if (!entry.startsWith("mechancete:")) return false
                val choice = decision.choices.firstOrNull { it.id.value == value } ?: return false
                decision.answer.complete(choice)
            }
        }
        return true
    }

    private fun push(request: DialogRequest) {
        if (outcome != null) return
        requests = requests + request
        refresh()
    }

    /** Drops the visible dialog without refreshing; the caller completes its decision and then refreshes. */
    private fun dropTopRequest() {
        if (outcome != null) return
        requests = requests.dropLast(1)
    }

    private fun pop() {
        if (outcome != null) return
        requests = requests.dropLast(1)
        refresh()
    }

    private fun replaceTop(request: DialogRequest) {
        if (outcome != null) return
        requests = requests.dropLast(1) + request
        refresh()
    }

    /** Ends the match with the game over as the only dialog; the caller refreshes. */
    private fun endMatch(result: Outcome) {
        outcome = result
        pending = null
        elyseeBanner = false
        requests = listOf(DialogRequest.GameOver)
    }

    private fun refresh() {
        // Cards can damage the human without an elimination event, so every refresh checks the human.
        if (outcome == null && !humanPlayer.isAlive && !engine.isGameOver) {
            gameJob?.cancel()
            endMatch(Outcome(winner = null, reason = null))
        }
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
        // The shop button says whether the human can buy something now, before the shop is opened.
        val shopAvailability = when (decision) {
            is HumanDecision.Shop -> if (decision.offers.any { it.cost <= humanPlayer.energy }) {
                ShopAvailability.AFFORDABLE
            } else {
                ShopAvailability.OPEN
            }
            else -> ShopAvailability.LOCKED
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
            shopAvailability = shopAvailability,
            elyseeBanner = elyseeBanner,
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
        DialogRequest.History -> MatchDialog.History(history.entries)
        DialogRequest.GameOver -> {
            val result = checkNotNull(outcome)
            gameOverDialog(engine, humanPlayer.id, result.winner, result.reason)
        }
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
        // Each AI decision waits this long, so the player can follow the AI turns.
        const val AI_DELAY_MILLIS = 1_200L

        // The turn result stays up longer than a single decision: 3 x 1 200 ms = 3.6 s.
        private const val AI_RESULT_FACTOR = 3

        private const val KEY_SEED = "match.seed"
        private const val KEY_ANSWERS = "match.answers"

        fun factory(human: Candidate): ViewModelProvider.Factory = viewModelFactory {
            initializer { MatchViewModel(human, createSavedStateHandle()) }
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
    data object History : DialogRequest
    data class CardDetail(val card: PowerCardUi) : DialogRequest
    data object Yield : DialogRequest
    data object Mechancete : DialogRequest
    data object GameOver : DialogRequest
}

/** How the match ended; [winner] is null when the human was eliminated. */
private class Outcome(val winner: Player?, val reason: WinReason?)
