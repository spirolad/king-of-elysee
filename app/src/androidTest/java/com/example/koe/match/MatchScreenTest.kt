package com.example.koe.match

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.koe.match.dialog.MatchDialog
import com.example.koe.match.dialog.MatchDialogActions
import com.example.koe.match.dialog.mockPauseDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun setScreen(
        dialogs: List<MatchDialog>,
        onPause: () -> Unit,
    ) {
        composeTestRule.setContent {
            MatchScreen(
                state = mockMatchUiState(),
                dialogs = dialogs,
                onPauseClick = onPause,
                onOpponentClick = {},
                onShopClick = {},
                onCardsClick = {},
                onHistoryClick = {},
                onEndTurnClick = {},
                onDieClick = {},
                onRollClick = {},
                onValidateDiceClick = {},
                onElyseeBannerDismiss = {},
                dialogActions = MatchDialogActions(),
            )
        }
    }

    private fun pressBack() {
        composeTestRule.runOnUiThread { composeTestRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeTestRule.waitForIdle()
    }

    @Test
    fun backOnBareBoardPauses() {
        var pauseCount = 0
        setScreen(dialogs = emptyList(), onPause = { pauseCount++ })

        pressBack()

        assertEquals("pause clicks from back", 1, pauseCount)
    }

    @Test
    fun backWithPauseDialogOpenDoesNotPause() {
        var pauseCount = 0
        setScreen(dialogs = listOf(mockPauseDialog()), onPause = { pauseCount++ })

        pressBack()

        assertEquals("pause clicks from back", 0, pauseCount)
    }
}
