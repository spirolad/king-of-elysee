package com.example.koe.match

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.koe.R

enum class Candidate(
    @param:StringRes val nameRes: Int,
    @param:DrawableRes val portrait: Int?,
    @param:ColorRes val avatarColor: Int,
    // White silhouette on dark discs, ink silhouette otherwise.
    val lightSilhouette: Boolean,
) {
    MACRON(R.string.character_macron_name, R.drawable.macron, R.color.koe_avatar_macron, lightSilhouette = false),
    MELENCHON(R.string.character_melenchon_name, R.drawable.melanchon, R.color.koe_avatar_melenchon, lightSilhouette = false),
    LEPEN(R.string.character_lepen_name, R.drawable.lepen, R.color.koe_avatar_lepen, lightSilhouette = true),
    POUTOU(R.string.character_poutou_name, R.drawable.poutou, R.color.koe_avatar_poutou, lightSilhouette = false),
}

@Immutable
data class OpponentUi(
    val candidate: Candidate,
    val popularite: Int,
    val votes: Int,
    val isKing: Boolean,
    // Their turn is being played.
    val isPlaying: Boolean = false,
)

@Immutable
data class MeUi(
    val candidate: Candidate,
    val votes: Int,
    val popularite: Int,
    val fonds: Int,
    val mechancete: Int,
)

sealed interface DieFace {
    data class Number(val value: Int) : DieFace
    data object Tweet : DieFace
    data object Fonds : DieFace
    data object Popularite : DieFace
}

@Immutable
data class DieUi(
    val face: DieFace,
    val highlighted: Boolean,
)

@Immutable
data class MatchUiState(
    val opponents: List<OpponentUi>,
    // null while nobody holds the Élysée.
    val king: Candidate?,
    val me: MeUi,
    // Whose turn it is; the title addresses the player only on their own turn.
    val currentPlayer: Candidate,
    val dice: List<DieUi>,
    // null hides the result pill.
    val turnResult: TurnResultUi?,
    val cardsCount: Int,
    // false hides FIN DU TOUR.
    val canEndTurn: Boolean,
    // Floating "+3" beside the title; null hides it.
    val votesDelta: Int? = null,
    // Tweet banner under the target chip; null hides it.
    val tweetBanner: TweetBannerUi? = null,
    // Non-null while the human rolls; the dice are tappable once thrown.
    val roll: RollUi? = null,
)

@Immutable
data class TweetBannerUi(
    val target: Candidate,
    val popularityLoss: Int,
)

/** The human's roll phase: [rollNumber] is 0 before the first throw. */
@Immutable
data class RollUi(
    val rollNumber: Int,
    val maxRolls: Int,
)

/** What the current player's dice did this turn. */
@Immutable
data class TurnResultUi(
    val votes: Int,
    val fonds: Int,
    val popularite: Int,
    val tweets: Int,
    // Candidates hit by the tweets, in order.
    val targets: List<Candidate>,
)
