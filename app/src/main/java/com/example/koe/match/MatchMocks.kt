package com.example.koe.match

// Mock data for the Figma frame, for previews and UI tests.
fun mockMatchUiState(): MatchUiState = MatchUiState(
    opponents = listOf(
        OpponentUi(Candidate.MELENCHON, popularite = 8, votes = 11, isKing = false),
        OpponentUi(Candidate.LEPEN, popularite = 6, votes = 17, isKing = true),
        OpponentUi(Candidate.POUTOU, popularite = 3, votes = 6, isKing = false),
    ),
    king = Candidate.LEPEN,
    me = MeUi(
        candidate = Candidate.MACRON,
        votes = 20,
        popularite = 7,
        fonds = 5,
        // Figma shows 2; 3 reaches the first tier so FIN DU TOUR opens the Méchanceté dialog.
        mechancete = 3,
    ),
    currentPlayer = Candidate.MACRON,
    dice = listOf(
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Tweet, highlighted = false),
        DieUi(DieFace.Tweet, highlighted = false),
        DieUi(DieFace.Fonds, highlighted = false),
    ),
    turnResult = TurnResultUi(votes = 3, fonds = 1, popularite = 0, tweets = 2, targets = listOf(Candidate.LEPEN)),
    cardsCount = 3,
    canEndTurn = true,
    votesDelta = 3,
    tweetBanner = TweetBannerUi(Candidate.LEPEN, 2),
)

fun mockRollingUiState(rollNumber: Int = 1): MatchUiState = mockMatchUiState().copy(
    roll = RollUi(rollNumber, maxRolls = 3),
    canEndTurn = false,
    turnResult = null,
    votesDelta = null,
    tweetBanner = null,
)

fun mockElyseeBannerUiState(): MatchUiState = mockMatchUiState().copy(elyseeBanner = true)
