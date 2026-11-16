package com.example.koe.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.koe.R
import com.example.koe.match.Candidate
import com.example.koe.match.TurnIntro
import com.example.koe.match.TurnIntroUi
import com.example.koe.ui.components.KoeAdaptiveFrame

// 10" landscape tablet, as the screens are hosted by KoeComposeFragment.

@Composable
private fun Tablet(content: @Composable () -> Unit) =
    KoeAdaptiveFrame(background = colorResource(R.color.koe_screen_home_container), content = content)

@Preview(name = "Tablet · title", widthDp = 1280, heightDp = 800)
@Composable
private fun TitleTabletPreview() = Tablet { TitleScreen(onStart = {}) }

@Preview(name = "Tablet · menu", widthDp = 1280, heightDp = 800)
@Composable
private fun MenuTabletPreview() = Tablet { MainMenuScreen(onPlay = {}, onRules = {}, onSettings = {}) }

@Preview(name = "Tablet · rules", widthDp = 1280, heightDp = 800)
@Composable
private fun RulesTabletPreview() = Tablet { RulesScreen(onBack = {}) }

@Preview(name = "Tablet · settings", widthDp = 1280, heightDp = 800)
@Composable
private fun SettingsTabletPreview() = Tablet { SettingsScreen(onBack = {}, onRules = {}) }

@Preview(name = "Tablet · character", widthDp = 1280, heightDp = 800)
@Composable
private fun CharacterTabletPreview() = Tablet {
    CharacterScreen(selected = Candidate.MACRON, onSelect = {}, onValidate = {})
}

@Preview(name = "Tablet · ready", widthDp = 1280, heightDp = 800)
@Composable
private fun ReadyTabletPreview() = Tablet {
    ReadyScreen(
        order = listOf(Candidate.MELENCHON, Candidate.MACRON, Candidate.POUTOU, Candidate.LEPEN),
        human = Candidate.MACRON,
        onBack = {},
        onStart = {},
    )
}

@Preview(name = "Tablet · turn intro", widthDp = 1280, heightDp = 800)
@Composable
private fun TurnIntroTabletPreview() = TurnIntro(
    candidate = Candidate.MACRON,
    intro = TurnIntroUi(round = 9, votes = 14, popularite = 7, fonds = 5),
    onStart = {},
)
