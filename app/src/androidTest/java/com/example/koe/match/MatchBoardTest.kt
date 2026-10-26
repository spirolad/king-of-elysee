package com.example.koe.match

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

private const val FRAME_TOLERANCE = 0.75f

@RunWith(AndroidJUnit4::class)
class MatchBoardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The board is designed for an 844 x 390 dp frame. Override the density so the frame fits any screen.
     */
    private val frameDensity: Density by lazy {
        val metrics = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics
        Density(
            density = minOf(metrics.widthPixels / 844f, metrics.heightPixels / 390f),
            fontScale = 1f,
        )
    }

    private fun setFrame(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides frameDensity) {
                Box(Modifier.requiredSize(844.dp, 390.dp)) {
                    content()
                }
            }
        }
    }

    private fun setBoard(
        state: MatchUiState = mockMatchUiState(),
        onPauseClick: () -> Unit = {},
        onOpponentClick: (Candidate) -> Unit = {},
        onShopClick: () -> Unit = {},
        onCardsClick: () -> Unit = {},
        onEndTurnClick: () -> Unit = {},
        onDieClick: (Int) -> Unit = {},
        onRollClick: () -> Unit = {},
        onValidateDiceClick: () -> Unit = {},
    ) {
        setFrame {
            MatchBoard(
                state = state,
                onPauseClick = onPauseClick,
                onOpponentClick = onOpponentClick,
                onShopClick = onShopClick,
                onCardsClick = onCardsClick,
                onEndTurnClick = onEndTurnClick,
                onDieClick = onDieClick,
                onRollClick = onRollClick,
                onValidateDiceClick = onValidateDiceClick,
            )
        }
    }

    /** Bounds in frame dp. The rule's own position assertions would use the device density instead. */
    private fun SemanticsNodeInteraction.frameBounds(): Rect {
        val px = fetchSemanticsNode().boundsInRoot
        val d = frameDensity.density
        return Rect(px.left / d, px.top / d, px.right / d, px.bottom / d)
    }

    private fun SemanticsNodeInteraction.assertFrame(
        tag: String,
        left: Float,
        top: Float,
        width: Float? = null,
        height: Float? = null,
    ) {
        val b = frameBounds()
        assertTrue("$tag left: expected $left, was ${b.left}", abs(b.left - left) <= FRAME_TOLERANCE)
        assertTrue("$tag top: expected $top, was ${b.top}", abs(b.top - top) <= FRAME_TOLERANCE)
        if (width != null) {
            assertTrue("$tag width: expected $width, was ${b.width}", abs(b.width - width) <= FRAME_TOLERANCE)
        }
        if (height != null) {
            assertTrue("$tag height: expected $height, was ${b.height}", abs(b.height - height) <= FRAME_TOLERANCE)
        }
    }

    private fun frame(tag: String) = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    // Layout, in Figma frame dp

    @Test
    fun topRowMatchesFigma() {
        setBoard()

        frame(MatchBoardTags.PAUSE).assertFrame(MatchBoardTags.PAUSE, 47f, 14f, 52f, 52f)
        frame(MatchBoardTags.chip(Candidate.MELENCHON))
            .assertFrame(MatchBoardTags.chip(Candidate.MELENCHON), 111f, 12f, 220.67f, 56f)
        frame(MatchBoardTags.chip(Candidate.LEPEN))
            .assertFrame(MatchBoardTags.chip(Candidate.LEPEN), 343.67f, 12f, 220.67f, 56f)
        frame(MatchBoardTags.chip(Candidate.POUTOU))
            .assertFrame(MatchBoardTags.chip(Candidate.POUTOU), 576.33f, 12f, 220.67f, 56f)
    }

    @Test
    fun centerColumnMatchesFigma() {
        setBoard()

        frame(MatchBoardTags.DICE_ROW).assertFrame(MatchBoardTags.DICE_ROW, 224f, 179f, 384f, 64f)
        frame(MatchBoardTags.die(0)).assertFrame(MatchBoardTags.die(0), 228f, 183f, 56f, 56f)
        frame(MatchBoardTags.die(5)).assertFrame(MatchBoardTags.die(5), 548f, 183f, 56f, 56f)

        val result = frame(MatchBoardTags.RESULT).frameBounds()
        val centerX = result.left + result.width / 2f
        assertTrue(
            "${MatchBoardTags.RESULT} centre x: expected 416.0, was $centerX",
            abs(centerX - 416f) <= FRAME_TOLERANCE,
        )
        assertTrue(
            "${MatchBoardTags.RESULT} top: expected 255.0, was ${result.top}",
            abs(result.top - 255f) <= FRAME_TOLERANCE,
        )
        assertTrue(
            "${MatchBoardTags.RESULT} height: expected 36.0, was ${result.height}",
            abs(result.height - 36f) <= FRAME_TOLERANCE,
        )
    }

    @Test
    fun panelsMatchFigma() {
        setBoard()

        frame(MatchBoardTags.KING_PANEL).assertFrame(MatchBoardTags.KING_PANEL, 47f, 83f, 148f)
        frame(MatchBoardTags.ME_PANEL).assertFrame(MatchBoardTags.ME_PANEL, 637f, 84f, 160f, 205f)
    }

    @Test
    fun bottomRowMatchesFigma() {
        setBoard()

        frame(MatchBoardTags.SHOP).assertFrame(MatchBoardTags.SHOP, 47f, 317f, 158.6f, 52f)
        frame(MatchBoardTags.CARDS).assertFrame(MatchBoardTags.CARDS, 217.6f, 317f, 162.9f, 52f)
        frame(MatchBoardTags.END_TURN).assertFrame(MatchBoardTags.END_TURN, 580.7f, 305f, 216.3f, 64f)
    }

    // Content

    @Test
    fun titleAndResultAreShown() {
        setBoard()

        composeTestRule.onNodeWithText("À TOI, MACRON").assertIsDisplayed()
        composeTestRule.onNodeWithText("Résultat du tour").assertIsDisplayed()
        composeTestRule.onNodeWithText("+3 Votes, +1 Fonds, 2 Tweets sur Le Pen").assertIsDisplayed()
        composeTestRule.onNodeWithText("AU POUVOIR").assertIsDisplayed()
    }

    @Test
    fun theirTurnTitleNamesThePlayingCandidate() {
        setBoard(state = mockMatchUiState().copy(currentPlayer = Candidate.LEPEN))

        composeTestRule.onNodeWithText("AU TOUR DE LE PEN").assertIsDisplayed()
        composeTestRule.onNodeWithText("À TOI, MACRON").assertDoesNotExist()
    }

    @Test
    fun playingOpponentChipSaysItIsPlaying() {
        val state = mockMatchUiState().copy(
            opponents = mockMatchUiState().opponents.map {
                if (it.candidate == Candidate.LEPEN) it.copy(isPlaying = true) else it
            },
        )
        setBoard(state = state)

        composeTestRule.onNodeWithTag(MatchBoardTags.chip(Candidate.LEPEN))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "en train de jouer"))
        composeTestRule.onNodeWithTag(MatchBoardTags.chip(Candidate.MELENCHON))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
    }

    @Test
    fun eliminatedOpponentChipSaysItIsEliminated() {
        val state = mockMatchUiState().copy(
            opponents = mockMatchUiState().opponents.map {
                if (it.candidate == Candidate.POUTOU) it.copy(isEliminated = true) else it
            },
        )
        setBoard(state = state)

        composeTestRule.onNodeWithTag(MatchBoardTags.chip(Candidate.POUTOU))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "éliminé"))
    }

    @Test
    fun resultPillHiddenWithoutResult() {
        setBoard(state = mockMatchUiState().copy(turnResult = null))

        composeTestRule.onNodeWithTag(MatchBoardTags.RESULT).assertDoesNotExist()
    }

    @Test
    fun endTurnHiddenWhenTurnCannotEnd() {
        setBoard(state = mockMatchUiState().copy(canEndTurn = false))

        composeTestRule.onNodeWithTag(MatchBoardTags.END_TURN).assertDoesNotExist()
    }

    @Test
    fun endTurnAppearsWhenTurnCanEnd() {
        var state by mutableStateOf(mockMatchUiState().copy(canEndTurn = false))
        setFrame {
            MatchBoard(
                state = state,
                onPauseClick = {},
                onOpponentClick = {},
                onShopClick = {},
                onCardsClick = {},
                onEndTurnClick = {},
                onDieClick = {},
                onRollClick = {},
                onValidateDiceClick = {},
            )
        }
        composeTestRule.onNodeWithTag(MatchBoardTags.END_TURN).assertDoesNotExist()

        composeTestRule.runOnIdle { state = state.copy(canEndTurn = true) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(MatchBoardTags.END_TURN).assertIsDisplayed()
    }

    @Test
    fun diceDescribeTheirFaces() {
        setBoard()

        composeTestRule.onNodeWithContentDescription("Dé 1 : 3").assertExists()
        composeTestRule.onNodeWithContentDescription("Dé 4 : Tweet").assertExists()
        composeTestRule.onNodeWithContentDescription("Dé 6 : Fonds publics").assertExists()
    }

    @Test
    fun kingChipAnnouncesPower() {
        setBoard()

        composeTestRule.onNodeWithContentDescription("Le Pen, 6 Popularité, 17 Votes, au pouvoir").assertExists()
    }

    // Callbacks

    @Test
    fun pauseButtonInvokesCallback() {
        var pauseCount = 0
        setBoard(onPauseClick = { pauseCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.PAUSE).performClick()

        assertEquals("pause clicks", 1, pauseCount)
    }

    @Test
    fun opponentChipInvokesCallbackWithCandidate() {
        var clicked: Candidate? = null
        setBoard(onOpponentClick = { clicked = it })

        composeTestRule.onNodeWithTag(MatchBoardTags.chip(Candidate.MELENCHON)).performClick()

        assertEquals("opponent click", Candidate.MELENCHON, clicked)
    }

    @Test
    fun shopButtonInvokesCallback() {
        var shopCount = 0
        setBoard(onShopClick = { shopCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.SHOP).performClick()

        assertEquals("shop clicks", 1, shopCount)
    }

    @Test
    fun cardsButtonInvokesCallback() {
        var cardsCount = 0
        setBoard(onCardsClick = { cardsCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.CARDS).performClick()

        assertEquals("cards clicks", 1, cardsCount)
    }

    @Test
    fun endTurnButtonInvokesCallback() {
        var endTurnCount = 0
        setBoard(onEndTurnClick = { endTurnCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.END_TURN).performClick()

        assertEquals("end turn clicks", 1, endTurnCount)
    }

    // Dice rolling

    @Test
    fun rollPromptAndRollButtonShownBeforeFirstThrow() {
        setBoard(state = mockRollingUiState(rollNumber = 0))

        composeTestRule.onNodeWithText("Lance les dés pour commencer ton tour.").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MatchBoardTags.ROLL).assertIsDisplayed()
        composeTestRule.onNodeWithTag(MatchBoardTags.END_TURN).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MatchBoardTags.VALIDATE_DICE).assertDoesNotExist()
    }

    @Test
    fun diceIgnoreClicksBeforeFirstThrow() {
        var dieClicks = 0
        setBoard(state = mockRollingUiState(rollNumber = 0), onDieClick = { dieClicks++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.die(2)).performClick()

        assertEquals("die clicks", 0, dieClicks)
    }

    @Test
    fun rollCountAndValidateShownAfterThrow() {
        setBoard(state = mockRollingUiState(rollNumber = 1))

        composeTestRule.onNodeWithText("Lancer 1 sur 3 · touche un dé pour le garder").assertIsDisplayed()
        composeTestRule.onNodeWithTag(MatchBoardTags.VALIDATE_DICE).assertIsDisplayed()
    }

    @Test
    fun dieInvokesCallbackWithIndexAfterThrow() {
        var clicked: Int? = null
        setBoard(state = mockRollingUiState(rollNumber = 1), onDieClick = { clicked = it })

        composeTestRule.onNodeWithTag(MatchBoardTags.die(2)).performClick()

        assertEquals("die click", 2, clicked)
    }

    @Test
    fun rollButtonInvokesCallback() {
        var rollCount = 0
        setBoard(state = mockRollingUiState(rollNumber = 1), onRollClick = { rollCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.ROLL).performClick()

        assertEquals("roll clicks", 1, rollCount)
    }

    @Test
    fun validateButtonInvokesCallback() {
        var validateCount = 0
        setBoard(state = mockRollingUiState(rollNumber = 1), onValidateDiceClick = { validateCount++ })

        composeTestRule.onNodeWithTag(MatchBoardTags.VALIDATE_DICE).performClick()

        assertEquals("validate clicks", 1, validateCount)
    }

    @Test
    fun resultPillSaysNothingWhenNoEffect() {
        setBoard(state = mockMatchUiState().copy(turnResult = TurnResultUi(0, 0, 0, 0, emptyList())))

        composeTestRule.onNodeWithText("Rien ce tour").assertIsDisplayed()
    }

    @Test
    fun resultPillListsPopulariteAndTweetsWithoutTargets() {
        setBoard(
            state = mockMatchUiState().copy(
                turnResult = TurnResultUi(votes = 0, fonds = 0, popularite = 2, tweets = 1, targets = emptyList()),
            ),
        )

        composeTestRule.onNodeWithText("+2 Popularité, 1 Tweet").assertIsDisplayed()
    }
}
