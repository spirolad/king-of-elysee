package com.example.koe.match

// Mock data for the Figma frame. Delete once the engine feeds real values.
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
        mechancete = 2,
    ),
    dice = listOf(
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Number(3), highlighted = true),
        DieUi(DieFace.Tweet, highlighted = false),
        DieUi(DieFace.Tweet, highlighted = false),
        DieUi(DieFace.Fonds, highlighted = false),
    ),
    turnResult = "+3 Votes, +1 Fonds, 2 Tweets sur Le Pen",
    cardsCount = 3,
    canEndTurn = true,
    votesDelta = 3,
    tweetBanner = TweetBannerUi(Candidate.LEPEN, 2),
)
