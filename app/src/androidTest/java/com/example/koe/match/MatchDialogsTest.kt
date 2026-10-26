package com.example.koe.match

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.MatchDialogActions
import com.example.koe.match.dialog.MatchDialogHost
import com.example.koe.match.dialog.GameOverReason
import com.example.koe.match.dialog.MatchDialogTags
import com.example.koe.match.dialog.PowerCardUi
import com.example.koe.match.dialog.mockCardDetailDialog
import com.example.koe.match.dialog.mockHistoryDialog
import com.example.koe.match.dialog.mockGameOverDialog
import com.example.koe.match.dialog.mockLockedShopDialog
import com.example.koe.match.dialog.mockMechanceteDialog
import com.example.koe.match.dialog.mockMyCardsDialog
import com.example.koe.match.dialog.mockPauseDialog
import com.example.koe.match.dialog.mockPlayerDetailsDialog
import com.example.koe.match.dialog.mockQuitDialog
import com.example.koe.match.dialog.mockShopDialog
import com.example.koe.match.dialog.mockYieldDialog
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
class MatchDialogsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * The dialogs are designed for an 844 x 390 dp frame. Override the density so the frame fits any screen.
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

    private fun setDialogs(
        dialogs: List<MatchDialog>,
        actions: MatchDialogActions = MatchDialogActions(),
    ) {
        setFrame {
            MatchDialogHost(dialogs = dialogs, actions = actions)
        }
    }

    /** Bounds in frame dp. The rule's own position assertions would use the device density instead. */
    private fun SemanticsNodeInteraction.frameBounds(): Rect {
        val px = fetchSemanticsNode().boundsInRoot
        val d = frameDensity.density
        return Rect(px.left / d, px.top / d, px.right / d, px.bottom / d)
    }

    private fun SemanticsNodeInteraction.assertFrame(
        name: String,
        left: Float,
        top: Float,
        width: Float? = null,
        height: Float? = null,
    ) {
        val b = frameBounds()
        assertTrue("$name left: expected $left, was ${b.left}", abs(b.left - left) <= FRAME_TOLERANCE)
        assertTrue("$name top: expected $top, was ${b.top}", abs(b.top - top) <= FRAME_TOLERANCE)
        if (width != null) {
            assertTrue("$name width: expected $width, was ${b.width}", abs(b.width - width) <= FRAME_TOLERANCE)
        }
        if (height != null) {
            assertTrue("$name height: expected $height, was ${b.height}", abs(b.height - height) <= FRAME_TOLERANCE)
        }
    }

    private fun frame(tag: String) = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun pressBack() {
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    private fun clickScrim() {
        composeTestRule.onAllNodesWithTag(MatchDialogTags.SCRIM).onLast()
            .performTouchInput { click(Offset(10f, 10f)) }
    }

    // Layout, in Figma frame dp

    @Test
    fun pausePanelMatchesFigma() {
        setDialogs(listOf(mockPauseDialog()))

        frame(MatchDialogTags.PAUSE).assertFrame(MatchDialogTags.PAUSE, 142f, 52f, 560f, 286f)
        composeTestRule.onNodeWithText("REPRENDRE").assertFrame("REPRENDRE", 468f, 76f, 210f, 52f)
        composeTestRule.onNodeWithText("QUITTER").assertFrame("QUITTER", 468f, 268f, 210f, 52f)
    }

    @Test
    fun quitPanelMatchesFigma() {
        setDialogs(listOf(mockQuitDialog()))

        frame(MatchDialogTags.QUIT).assertFrame(MatchDialogTags.QUIT, 192f, 89f, 460f, 212f)
        composeTestRule.onNodeWithText("ANNULER").assertFrame("ANNULER", 216f, 225f, 200f, 52f)
        composeTestRule.onNodeWithText("QUITTER").assertFrame("QUITTER", 428f, 225f, 200f, 52f)
    }

    @Test
    fun yieldPanelMatchesFigma() {
        setDialogs(listOf(mockYieldDialog()))

        frame(MatchDialogTags.YIELD).assertFrame(MatchDialogTags.YIELD, 142f, 41f, 560f, 308f)
        composeTestRule.onNodeWithText("RESTER").assertFrame("RESTER", 166f, 243f, 250f, 52f)
        composeTestRule.onNodeWithText("CÉDER").assertFrame("CÉDER", 428f, 243f, 250f, 52f)
    }

    @Test
    fun mechancetePanelMatchesFigma() {
        setDialogs(listOf(mockMechanceteDialog()))

        frame(MatchDialogTags.MECHANCETE).assertFrame(MatchDialogTags.MECHANCETE, 82f, 35f, 680f, 320f)
        composeTestRule.onNodeWithText("VALIDER").assertFrame("VALIDER", 612.8f, 57f, 125.3f, 52f)
        frame(MatchDialogTags.card("petite_phrase_1"))
            .assertFrame(MatchDialogTags.card("petite_phrase_1"), 185f, 128f, 150f, 200f)
    }

    @Test
    fun cardDetailPanelMatchesFigma() {
        setDialogs(listOf(mockCardDetailDialog()))

        frame(MatchDialogTags.CARD_DETAIL).assertFrame(MatchDialogTags.CARD_DETAIL, 100f, 42f, 644f, 306f)
        composeTestRule.onNodeWithText("OK").assertFrame("OK", 639.4f, 276f, 80.6f, 52f)
    }

    @Test
    fun playerPanelMatchesFigma() {
        setDialogs(listOf(mockPlayerDetailsDialog()))

        frame(MatchDialogTags.PLAYER).assertFrame(MatchDialogTags.PLAYER, 17f, 36f, 810f, 354f)
        frame(MatchDialogTags.CLOSE).assertFrame(MatchDialogTags.CLOSE, 767f, 52f, 44f, 44f)
        frame(MatchDialogTags.STATS).assertFrame(MatchDialogTags.STATS, 177f, 56f, 577f)
        frame(MatchDialogTags.card("meeting_1"))
            .assertFrame(MatchDialogTags.card("meeting_1"), 177f, 169f, 150f, 200f)
    }

    @Test
    fun shopPanelMatchesFigma() {
        setDialogs(listOf(mockShopDialog()))

        frame(MatchDialogTags.SHOP).assertFrame(MatchDialogTags.SHOP, 47f, 79f, 750f, 311f)
        frame(MatchDialogTags.CLOSE).assertFrame(MatchDialogTags.CLOSE, 729f, 97f, 44f, 44f)
        frame(MatchDialogTags.card("fausse_info"))
            .assertFrame(MatchDialogTags.card("fausse_info"), 71f, 156f, 150f, 200f)
        frame(MatchDialogTags.CHOICE).assertFrame(MatchDialogTags.CHOICE, 565f, 153f, 208f, 98f)
        composeTestRule.onNodeWithText("ACHETER").assertFrame("ACHETER", 565f, 261f, 208f, 44f)
        composeTestRule.onNodeWithText("RENOUVELER").assertFrame("RENOUVELER", 565f, 315f, 208f, 44f)
    }

    @Test
    fun myCardsPanelMatchesFigma() {
        setDialogs(listOf(mockMyCardsDialog()))

        frame(MatchDialogTags.MY_CARDS).assertFrame(MatchDialogTags.MY_CARDS, 47f, 86f, 750f, 304f)
        frame(MatchDialogTags.CLOSE).assertFrame(MatchDialogTags.CLOSE, 729f, 104f, 44f, 44f)
        frame(MatchDialogTags.card("meeting_1"))
            .assertFrame(MatchDialogTags.card("meeting_1"), 71f, 164f, 150f, 200f)
        frame(MatchDialogTags.MECHANCETE_PANEL)
            .assertFrame(MatchDialogTags.MECHANCETE_PANEL, 565f, 160f, 208f, 206f)
    }

    // Pause

    @Test
    fun pauseButtonsInvokeCallbacksOnce() {
        var resumeCount = 0
        var rulesCount = 0
        var settingsCount = 0
        var quitRequestCount = 0
        setDialogs(
            listOf(mockPauseDialog()),
            MatchDialogActions(
                onResume = { resumeCount++ },
                onRules = { rulesCount++ },
                onSettings = { settingsCount++ },
                onQuitRequest = { quitRequestCount++ },
            ),
        )

        composeTestRule.onNodeWithText("REPRENDRE").performClick()
        composeTestRule.onNodeWithText("RÈGLES").performClick()
        composeTestRule.onNodeWithText("RÉGLAGES").performClick()
        composeTestRule.onNodeWithText("QUITTER").performClick()

        assertEquals("resume clicks", 1, resumeCount)
        assertEquals("rules clicks", 1, rulesCount)
        assertEquals("settings clicks", 1, settingsCount)
        assertEquals("quit request clicks", 1, quitRequestCount)
    }

    @Test
    fun pauseIgnoresScrimAndBack() {
        var dismissCount = 0
        setDialogs(listOf(mockPauseDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        clickScrim()
        pressBack()

        assertEquals("pause dismiss calls", 0, dismissCount)
    }

    // Quit

    @Test
    fun quitCancelDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockQuitDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        composeTestRule.onNodeWithText("ANNULER").performClick()

        assertEquals("cancel dismiss calls", 1, dismissCount)
    }

    @Test
    fun quitConfirmInvokesQuitCallback() {
        var confirmCount = 0
        setDialogs(listOf(mockQuitDialog()), MatchDialogActions(onQuitConfirmed = { confirmCount++ }))

        composeTestRule.onNodeWithText("QUITTER").performClick()

        assertEquals("quit confirmed calls", 1, confirmCount)
    }

    @Test
    fun quitScrimDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockQuitDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        clickScrim()

        assertEquals("scrim dismiss calls", 1, dismissCount)
    }

    @Test
    fun quitBackDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockQuitDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        pressBack()

        assertEquals("back dismiss calls", 1, dismissCount)
    }

    // Yield

    @Test
    fun yieldTextsAreShown() {
        setDialogs(listOf(mockYieldDialog()))

        composeTestRule.onNodeWithText("À TOI DE DÉCIDER").assertExists()
        composeTestRule.onNodeWithText("CÉDER L'ÉLYSÉE ?").assertExists()
        composeTestRule.onNodeWithText("−2 Popularité").assertExists()
    }

    @Test
    fun yieldButtonsInvokeTheirCallbacks() {
        var stayCount = 0
        var yieldCount = 0
        setDialogs(
            listOf(mockYieldDialog()),
            MatchDialogActions(
                onStay = { stayCount++ },
                onYield = { yieldCount++ },
            ),
        )

        composeTestRule.onNodeWithText("RESTER").performClick()
        composeTestRule.onNodeWithText("CÉDER").performClick()

        assertEquals("stay clicks", 1, stayCount)
        assertEquals("yield clicks", 1, yieldCount)
    }

    @Test
    fun yieldIgnoresScrimAndBack() {
        var dismissCount = 0
        setDialogs(listOf(mockYieldDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        clickScrim()
        pressBack()

        assertEquals("yield dismiss calls", 0, dismissCount)
    }

    // Mechancete

    @Test
    fun mechanceteValidateStaysDisabledUntilCardIsSelected() {
        var validated: PowerCardUi? = null
        setDialogs(
            listOf(mockMechanceteDialog()),
            MatchDialogActions(onMechanceteValidate = { validated = it }),
        )

        composeTestRule.onNodeWithText("VALIDER").assertIsNotEnabled()

        composeTestRule.onNodeWithTag(MatchDialogTags.card("petite_phrase_2")).performClick()
        composeTestRule.onNodeWithText("VALIDER").assertIsEnabled()
        composeTestRule.onNodeWithText("VALIDER").performClick()

        assertEquals("validated card id", "petite_phrase_2", validated?.id)
    }

    @Test
    fun mechanceteLongPressOpensCardDetails() {
        var detailed: PowerCardUi? = null
        setDialogs(
            listOf(mockMechanceteDialog()),
            MatchDialogActions(onCardDetails = { detailed = it }),
        )

        composeTestRule.onNodeWithTag(MatchDialogTags.card("petite_phrase_1"))
            .performTouchInput { longClick() }

        assertEquals("detailed card id", "petite_phrase_1", detailed?.id)
    }

    // Card detail

    @Test
    fun cardDetailTextsAreShown() {
        setDialogs(listOf(mockCardDetailDialog()))

        composeTestRule.onNodeWithText("CUMUL DES MANDATS").assertExists()
        composeTestRule.onNodeWithText("CARTE POUVOIR").assertExists()
        composeTestRule.onNodeWithText("Payée 7 Fonds publics au tour 6").assertExists()
        composeTestRule.onNodeWithText("Effet permanent").assertExists()
    }

    @Test
    fun cardDetailOkDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockCardDetailDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        composeTestRule.onNodeWithText("OK").performClick()

        assertEquals("ok dismiss calls", 1, dismissCount)
    }

    // Player details

    @Test
    fun playerDetailsTextsAreShown() {
        setDialogs(listOf(mockPlayerDetailsDialog(candidate = Candidate.MELENCHON, isKing = true)))

        composeTestRule.onNodeWithText("MÉLENCHON").assertExists()
        composeTestRule.onNodeWithText("AU POUVOIR").assertExists()
        composeTestRule.onNodeWithText("Joueur 3").assertExists()
        composeTestRule.onNodeWithText("Ses cartes").assertExists()
    }

    @Test
    fun playerDetailsWithoutKingHasNoPowerBadge() {
        setDialogs(listOf(mockPlayerDetailsDialog(candidate = Candidate.MELENCHON, isKing = false)))

        composeTestRule.onNodeWithText("AU POUVOIR").assertDoesNotExist()
    }

    @Test
    fun playerCloseDismisses() {
        var dismissCount = 0
        setDialogs(
            listOf(mockPlayerDetailsDialog()),
            MatchDialogActions(onDismiss = { dismissCount++ }),
        )

        composeTestRule.onNodeWithTag(MatchDialogTags.CLOSE).performClick()

        assertEquals("close dismiss calls", 1, dismissCount)
    }

    // Shop

    @Test
    fun shopInitialChoiceIsFirstAffordableCard() {
        setDialogs(listOf(mockShopDialog()))

        composeTestRule.onNode(
            hasText("Fausse info") and hasAnyAncestor(hasTestTag(MatchDialogTags.CHOICE)),
            useUnmergedTree = true,
        ).assertExists()
        composeTestRule.onNode(
            hasText("Achetable · il te restera 0 Fonds.") and hasAnyAncestor(hasTestTag(MatchDialogTags.CHOICE)),
            useUnmergedTree = true,
        ).assertExists()
    }

    @Test
    fun shopDisabledCardKeepsChoice() {
        setDialogs(listOf(mockShopDialog()))

        composeTestRule.onNodeWithTag(MatchDialogTags.card("article_49_3")).performClick()

        composeTestRule.onNode(
            hasText("Fausse info") and hasAnyAncestor(hasTestTag(MatchDialogTags.CHOICE)),
            useUnmergedTree = true,
        ).assertExists()
        composeTestRule.onNode(
            hasText("Article 49.3") and hasAnyAncestor(hasTestTag(MatchDialogTags.CHOICE)),
            useUnmergedTree = true,
        ).assertDoesNotExist()
    }

    @Test
    fun shopBuyInvokesCallbackWithSelectedCard() {
        var bought: PowerCardUi? = null
        setDialogs(listOf(mockShopDialog()), MatchDialogActions(onBuy = { bought = it }))

        composeTestRule.onNodeWithText("ACHETER").performClick()

        assertEquals("bought card id", "fausse_info", bought?.id)
    }

    @Test
    fun shopRenewInvokesCallback() {
        var renewCount = 0
        setDialogs(listOf(mockShopDialog()), MatchDialogActions(onRenew = { renewCount++ }))

        composeTestRule.onNodeWithText("RENOUVELER").performClick()

        assertEquals("renew clicks", 1, renewCount)
    }

    @Test
    fun shopLongPressOpensCardDetails() {
        var detailed: PowerCardUi? = null
        setDialogs(listOf(mockShopDialog()), MatchDialogActions(onCardDetails = { detailed = it }))

        composeTestRule.onNodeWithTag(MatchDialogTags.card("meeting")).performTouchInput { longClick() }

        assertEquals("detailed card id", "meeting", detailed?.id)
    }

    @Test
    fun lockedShopShowsHint() {
        setDialogs(listOf(mockLockedShopDialog()))

        composeTestRule.onNodeWithText("Tu pourras acheter après tes lancers, pendant ton tour.").assertExists()
    }

    @Test
    fun lockedShopRenewDoesNotInvokeCallback() {
        var renewCount = 0
        setDialogs(listOf(mockLockedShopDialog()), MatchDialogActions(onRenew = { renewCount++ }))

        composeTestRule.onNodeWithText("RENOUVELER").assertIsNotEnabled()
        composeTestRule.onNodeWithText("RENOUVELER").performTouchInput { click() }

        assertEquals("renew clicks", 0, renewCount)
    }

    @Test
    fun lockedShopCardTapSelectsNothing() {
        setDialogs(listOf(mockLockedShopDialog()))

        composeTestRule.onNodeWithTag(MatchDialogTags.card("fausse_info")).performClick()

        composeTestRule.onNode(
            hasText("Fausse info") and hasAnyAncestor(hasTestTag(MatchDialogTags.CHOICE)),
            useUnmergedTree = true,
        ).assertDoesNotExist()
        composeTestRule.onNodeWithText("Tu pourras acheter après tes lancers, pendant ton tour.").assertExists()
    }

    // My cards

    @Test
    fun myCardsTextsAreShown() {
        setDialogs(listOf(mockMyCardsDialog()))

        composeTestRule.onNodeWithText("TES CARTES").assertExists()
        composeTestRule.onNodeWithText("Paliers à 3, 6 et 10. Chaque palier donne une carte Méchanceté.").assertExists()
    }

    @Test
    fun myCardsCloseDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockMyCardsDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        composeTestRule.onNodeWithTag(MatchDialogTags.CLOSE).performClick()

        assertEquals("close dismiss calls", 1, dismissCount)
    }

    // History

    @Test
    fun historyPanelMatchesFigma() {
        setDialogs(listOf(mockHistoryDialog()))

        frame(MatchDialogTags.HISTORY).assertFrame(MatchDialogTags.HISTORY, 47f, 86f, 750f, 304f)
        frame(MatchDialogTags.CLOSE).assertFrame(MatchDialogTags.CLOSE, 729f, 104f, 44f, 44f)
    }

    @Test
    fun historyTextsAreShown() {
        setDialogs(listOf(mockHistoryDialog()))

        composeTestRule.onNodeWithText("HISTORIQUE").assertExists()
        composeTestRule.onNodeWithText("Le Pen tweete sur Macron : −2 Popularité").assertExists()
    }

    @Test
    fun emptyHistoryShowsPlaceholder() {
        setDialogs(listOf(MatchDialog.History(entries = emptyList())))

        composeTestRule.onNodeWithText("Rien ne s'est encore passé.").assertExists()
    }

    @Test
    fun historyCloseDismisses() {
        var dismissCount = 0
        setDialogs(listOf(mockHistoryDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        composeTestRule.onNodeWithTag(MatchDialogTags.CLOSE).performClick()

        assertEquals("close dismiss calls", 1, dismissCount)
    }

    // Game over

    @Test
    fun gameOverWonShowsResultAndButtons() {
        setDialogs(listOf(mockGameOverDialog(youWon = true)))

        composeTestRule.onNodeWithText("VICTOIRE !").assertExists()
        composeTestRule.onNodeWithText("Macron atteint 20 Votes et prend l'Élysée.").assertExists()
        composeTestRule.onNodeWithTag(MatchDialogTags.REPLAY).assertExists()
        composeTestRule.onNodeWithTag(MatchDialogTags.GAME_OVER_QUIT).assertExists()
    }

    @Test
    fun gameOverReplayAndQuitInvokeCallbacks() {
        var replayCount = 0
        var quitCount = 0
        setDialogs(
            listOf(mockGameOverDialog()),
            MatchDialogActions(
                onReplay = { replayCount++ },
                onQuitConfirmed = { quitCount++ },
            ),
        )

        composeTestRule.onNodeWithTag(MatchDialogTags.REPLAY).performClick()
        composeTestRule.onNodeWithTag(MatchDialogTags.GAME_OVER_QUIT).performClick()

        assertEquals("replay clicks", 1, replayCount)
        assertEquals("quit confirmed clicks", 1, quitCount)
    }

    @Test
    fun gameOverIgnoresScrimAndBack() {
        var dismissCount = 0
        setDialogs(listOf(mockGameOverDialog()), MatchDialogActions(onDismiss = { dismissCount++ }))

        clickScrim()
        pressBack()

        assertEquals("game over dismiss calls", 0, dismissCount)
    }

    @Test
    fun gameOverEliminatedShowsDefeat() {
        setDialogs(
            listOf(
                mockGameOverDialog(youWon = false).copy(
                    winner = null,
                    reason = GameOverReason.ELIMINATED,
                ),
            ),
        )

        composeTestRule.onNodeWithText("DÉFAITE").assertExists()
        composeTestRule.onNodeWithText("Tu n'as plus de popularité : tu quittes la course.").assertExists()
    }

    // Stack

    @Test
    fun stackShowsBothDialogsAndBackDismissesTopOnce() {
        var dismissCount = 0
        setDialogs(
            listOf(mockShopDialog(), mockCardDetailDialog()),
            MatchDialogActions(onDismiss = { dismissCount++ }),
        )

        composeTestRule.onNodeWithTag(MatchDialogTags.SHOP).assertExists()
        composeTestRule.onNodeWithTag(MatchDialogTags.CARD_DETAIL).assertExists()

        pressBack()

        assertEquals("back dismiss calls", 1, dismissCount)
    }
}
