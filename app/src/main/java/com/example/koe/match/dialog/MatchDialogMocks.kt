package com.example.koe.match.dialog

import com.example.koe.match.Candidate

// Figma data for previews and UI tests.

fun mockMeetingGeant(
    owned: Boolean = false,
    id: String = "meeting",
    cost: Int = 5,
): PowerCardUi = PowerCardUi(
    id = id,
    title = "Meeting géant",
    description = "+1 Popularité au début de chacun de ses tours.",
    kind = CardKind.POUVOIR,
    cost = cost,
    owned = owned,
)

val mockPetitePhrase = PowerCardUi(
    id = "petite_phrase",
    title = "Petite phrase",
    description = "Ses Tweets font 1 dégât de plus.",
    kind = CardKind.MECHANCETE,
    cost = null,
)

val mockFausseInfo = PowerCardUi(
    id = "fausse_info",
    title = "Fausse info",
    description = "+1 Popularité au début de chacun de ses tours.",
    kind = CardKind.ACTION,
    cost = 5,
)

val mockArticle493 = PowerCardUi(
    id = "article_49_3",
    title = "Article 49.3",
    description = "+3 Votes, sans débat",
    kind = CardKind.ACTION,
    cost = 6,
    enabled = false,
)

val mockCumulDesMandats = PowerCardUi(
    id = "cumul",
    title = "Cumul des mandats",
    description = "Tu lances 7 dés au lieu de 6, à chaque tour, jusqu'à la fin de la partie.",
    kind = CardKind.POUVOIR,
    cost = 7,
    owned = true,
    paid = CardPaymentUi(cost = 7, turn = 6),
)

fun mockPauseDialog(): MatchDialog.Pause = MatchDialog.Pause(
    turn = 9,
    currentPlayer = Candidate.MACRON,
    ranking = listOf(
        RankingRowUi(Candidate.LEPEN, 17),
        RankingRowUi(Candidate.MACRON, 14),
        RankingRowUi(Candidate.MELENCHON, 11),
        RankingRowUi(Candidate.POUTOU, 6),
    ),
)

fun mockQuitDialog(): MatchDialog.QuitConfirm = MatchDialog.QuitConfirm(Candidate.LEPEN)

fun mockYieldDialog(): MatchDialog.YieldElysee = MatchDialog.YieldElysee(
    king = Candidate.LEPEN,
    attacker = Candidate.MACRON,
    popularityLoss = 2,
    popularityLeft = 4,
)

fun mockMechanceteDialog(): MatchDialog.Mechancete = MatchDialog.Mechancete(
    level = 3,
    choices = listOf(
        mockPetitePhrase.copy(id = "petite_phrase_1"),
        mockPetitePhrase.copy(id = "petite_phrase_2"),
        mockPetitePhrase.copy(id = "petite_phrase_3"),
    ),
)

fun mockCardDetailDialog(): MatchDialog.CardDetail = MatchDialog.CardDetail(mockCumulDesMandats)

fun mockPlayerDetailsDialog(
    candidate: Candidate = Candidate.MELENCHON,
    isKing: Boolean = true,
    popularite: Int = 6,
    votes: Int = 17,
): MatchDialog.PlayerDetails = MatchDialog.PlayerDetails(
    candidate = candidate,
    isKing = isKing,
    playerNumber = 3,
    popularite = popularite,
    votes = votes,
    fonds = 4,
    mechancete = 2,
    cards = listOf(
        mockMeetingGeant(owned = true, id = "meeting_1", cost = 4),
        mockMeetingGeant(owned = true, id = "meeting_2", cost = 4),
        mockPetitePhrase,
    ),
)

fun mockShopDialog(): MatchDialog.Shop = MatchDialog.Shop(
    fonds = 5,
    cards = listOf(
        mockFausseInfo,
        mockArticle493,
        mockMeetingGeant(owned = true, cost = 4),
    ),
    renewCost = 2,
)

fun mockMyCardsDialog(): MatchDialog.MyCards = MatchDialog.MyCards(
    cards = listOf(
        mockMeetingGeant(id = "meeting_1", cost = 5),
        mockMeetingGeant(id = "meeting_2", cost = 5),
        mockPetitePhrase,
    ),
    mechancete = 2,
    nextTier = 3,
)
