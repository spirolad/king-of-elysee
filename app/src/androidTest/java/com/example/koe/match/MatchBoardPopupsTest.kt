package com.example.koe.match

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

private const val BANNER_CENTRE_TOLERANCE = 3f

@RunWith(AndroidJUnit4::class)
class MatchBoardPopupsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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

    private fun setBoard(state: MatchUiState = mockMatchUiState()) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides frameDensity) {
                Box(Modifier.requiredSize(844.dp, 390.dp)) {
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
            }
        }
    }

    /** Bounds in frame dp. The rule's own position assertions would use the device density instead. */
    private fun SemanticsNodeInteraction.frameBounds(): Rect {
        val px = fetchSemanticsNode().boundsInRoot
        val d = frameDensity.density
        return Rect(px.left / d, px.top / d, px.right / d, px.bottom / d)
    }

    private fun frame(tag: String) = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    @Test
    fun votesDeltaAndTweetBannerAreShown() {
        setBoard()

        composeTestRule.onNodeWithTag(MatchBoardTags.VOTES_DELTA).assertExists()
        composeTestRule.onNodeWithText("+3").assertExists()
        composeTestRule.onNodeWithTag(MatchBoardTags.TWEET_BANNER).assertExists()
        composeTestRule.onNodeWithText("Le Pen −2 Popularité").assertExists()
    }

    @Test
    fun votesDeltaAndTweetBannerHiddenWithoutState() {
        setBoard(state = mockMatchUiState().copy(votesDelta = null, tweetBanner = null))

        composeTestRule.onNodeWithTag(MatchBoardTags.VOTES_DELTA).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MatchBoardTags.TWEET_BANNER).assertDoesNotExist()
    }

    @Test
    fun tweetBannerCentreMatchesFigma() {
        setBoard()

        val banner = frame(MatchBoardTags.TWEET_BANNER).frameBounds()
        val centreX = banner.left + banner.width / 2f
        val centreY = banner.top + banner.height / 2f
        assertTrue(
            "${MatchBoardTags.TWEET_BANNER} centre x: expected 521.4, was $centreX",
            abs(centreX - 521.4f) <= BANNER_CENTRE_TOLERANCE,
        )
        assertTrue(
            "${MatchBoardTags.TWEET_BANNER} centre y: expected 74.25, was $centreY",
            abs(centreY - 74.25f) <= BANNER_CENTRE_TOLERANCE,
        )
    }
}
