package com.example.koe.match.game

import com.example.koe.game.dice.DieFace as EngineDieFace
import com.example.koe.game.engine.GameEngine
import com.example.koe.game.engine.WinReason
import com.example.koe.game.player.Monster
import com.example.koe.game.player.Player
import com.example.koe.game.player.PlayerId
import com.example.koe.game.shop.CardCatalog
import com.example.koe.game.shop.CardType
import com.example.koe.game.shop.MechanceteDeck
import com.example.koe.game.shop.PowerCard
import com.example.koe.game.shop.ShopOffer
import com.example.koe.match.Candidate
import com.example.koe.match.DieFace
import com.example.koe.match.DieUi
import com.example.koe.match.MatchUiState
import com.example.koe.match.MeUi
import com.example.koe.match.OpponentUi
import com.example.koe.match.RollUi
import com.example.koe.match.ShopAvailability
import com.example.koe.match.TurnResultUi
import com.example.koe.match.TweetBannerUi
import com.example.koe.match.dialog.CardKind
import com.example.koe.match.dialog.CardPaymentUi
import com.example.koe.match.dialog.GameOverReason
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.PowerCardUi
import com.example.koe.match.dialog.RankingRowUi

private val wickednessIds = CardCatalog.createWickednessTiles().map { it.id }.toSet()

/** The engine player for [this] candidate; its id is the candidate's name. */
fun Candidate.toPlayer(): Player =
    // The engine's monster is not shown by the app.
    Player(id = PlayerId(name), name = name, monster = Monster.entries[ordinal % Monster.entries.size])

/** The candidate behind an engine player, read back from its id. */
val Player.candidate: Candidate get() = Candidate.valueOf(id.value)

/** Maps an engine die face to the face shown on the board. */
fun EngineDieFace.toUi(): DieFace = when (this) {
    EngineDieFace.ONE -> DieFace.Number(1)
    EngineDieFace.TWO -> DieFace.Number(2)
    EngineDieFace.THREE -> DieFace.Number(3)
    EngineDieFace.ENERGY -> DieFace.Fonds
    EngineDieFace.ATTACK -> DieFace.Tweet
    EngineDieFace.HEAL -> DieFace.Popularite
}

/** Next Méchanceté tier above [mechancete]; past the last tier the last one stays the goal. */
fun nextMechanceteTier(mechancete: Int): Int =
    MechanceteDeck.TIERS.firstOrNull { it > mechancete } ?: MechanceteDeck.TIERS.last()

/**
 * Maps a power card for the UI. [owner] is the player holding the card (null for market cards);
 * [cost] overrides the printed price.
 */
fun PowerCard.toUi(
    owner: Player? = null,
    cost: Int = this.cost,
    enabled: Boolean = true,
): PowerCardUi {
    val isMechancete = id in wickednessIds
    val payment = owner?.purchases?.lastOrNull { it.card.id == id }
    return PowerCardUi(
        id = id.value,
        title = name,
        description = description,
        kind = when {
            isMechancete -> CardKind.MECHANCETE
            type == CardType.KEEP -> CardKind.POUVOIR
            else -> CardKind.ACTION
        },
        cost = if (isMechancete) null else cost,
        owned = owner != null,
        enabled = enabled,
        paid = payment?.let { CardPaymentUi(cost = it.cost, turn = it.round) },
    )
}

/** Main screen state seen from the player [me]. */
fun boardState(
    engine: GameEngine,
    me: PlayerId,
    dice: List<EngineDieFace>,
    keptIndices: List<Int>,
    canEndTurn: Boolean,
    roll: RollUi? = null,
    turnResult: TurnResultUi? = null,
    votesDelta: Int? = null,
    tweetBanner: TweetBannerUi? = null,
    shopAvailability: ShopAvailability = ShopAvailability.LOCKED,
    elyseeBanner: Boolean = false,
): MatchUiState {
    val player = engine.players.first { it.id == me }
    return MatchUiState(
        opponents = engine.players.filter { it.id != me }.map { opponent ->
            OpponentUi(
                candidate = opponent.candidate,
                popularite = opponent.health,
                votes = opponent.victoryPoints,
                isKing = engine.tokyoBoard.isInsideTokyo(opponent),
                isPlaying = opponent.id == engine.currentPlayer.id && opponent.isAlive,
                isEliminated = !opponent.isAlive,
            )
        },
        king = engine.tokyoBoard.occupants.firstOrNull()?.candidate,
        me = MeUi(
            candidate = player.candidate,
            votes = player.victoryPoints,
            popularite = player.health,
            fonds = player.energy,
            mechancete = player.mechancete,
        ),
        currentPlayer = engine.currentPlayer.candidate,
        dice = dice.mapIndexed { index, face ->
            DieUi(face = face.toUi(), highlighted = index in keptIndices)
        },
        turnResult = turnResult,
        cardsCount = player.cards.size,
        canEndTurn = canEndTurn,
        votesDelta = votesDelta,
        tweetBanner = tweetBanner,
        roll = roll,
        shopAvailability = shopAvailability,
        elyseeBanner = elyseeBanner,
    )
}

/** Players by victory points, most first. */
private fun ranking(engine: GameEngine): List<RankingRowUi> =
    engine.players
        .sortedByDescending { it.victoryPoints }
        .map { RankingRowUi(candidate = it.candidate, votes = it.victoryPoints) }

/** Pause dialog with the turn and the ranking by victory points. */
fun pauseDialog(engine: GameEngine): MatchDialog.Pause =
    MatchDialog.Pause(
        turn = engine.roundNumber,
        currentPlayer = engine.currentPlayer.candidate,
        ranking = ranking(engine),
    )

/** End of match seen from [me]; a null [winner] means [me] was eliminated and the match stopped there. */
fun gameOverDialog(engine: GameEngine, me: PlayerId, winner: Player?, reason: WinReason?): MatchDialog.GameOver =
    MatchDialog.GameOver(
        winner = winner?.candidate,
        youWon = winner?.id == me,
        reason = when {
            winner == null -> GameOverReason.ELIMINATED
            reason is WinReason.LastMonsterStanding -> GameOverReason.LAST_STANDING
            else -> GameOverReason.VOTES
        },
        ranking = ranking(engine),
    )

/** Details of one candidate, with the cards they hold. */
fun playerDetailsDialog(engine: GameEngine, candidate: Candidate): MatchDialog.PlayerDetails {
    val player = engine.players.first { it.candidate == candidate }
    return MatchDialog.PlayerDetails(
        candidate = candidate,
        isKing = engine.tokyoBoard.isInsideTokyo(player),
        playerNumber = engine.players.indexOf(player) + 1,
        popularite = player.health,
        votes = player.victoryPoints,
        fonds = player.energy,
        mechancete = player.mechancete,
        cards = player.cards.map { it.toUi(owner = player) },
    )
}

/** The player's own cards and Méchanceté progress. */
fun myCardsDialog(me: Player): MatchDialog.MyCards =
    MatchDialog.MyCards(
        cards = me.cards.map { it.toUi(owner = me) },
        mechancete = me.mechancete,
        nextTier = nextMechanceteTier(me.mechancete),
    )

/** Market dialog; a card is selectable only when [canBuy] and affordable. */
fun shopDialog(
    me: Player,
    offers: List<ShopOffer>,
    sweepCost: Int,
    canBuy: Boolean,
): MatchDialog.Shop =
    MatchDialog.Shop(
        fonds = me.energy,
        cards = offers.map { offer ->
            offer.card.toUi(cost = offer.cost, enabled = canBuy && offer.cost <= me.energy)
        },
        renewCost = sweepCost,
        canBuy = canBuy,
    )

/** Choice of Méchanceté cards for [tier]. */
fun mechanceteDialog(tier: Int, choices: List<PowerCard>): MatchDialog.Mechancete =
    MatchDialog.Mechancete(
        level = tier,
        choices = choices.map { it.toUi() },
    )

/** Dialog shown when [me] is forced out of the Élysée by [attacker]. */
fun yieldDialog(me: Player, attacker: Player, damage: Int): MatchDialog.YieldElysee =
    MatchDialog.YieldElysee(
        king = me.candidate,
        attacker = attacker.candidate,
        popularityLoss = damage,
        popularityLeft = me.health,
    )
