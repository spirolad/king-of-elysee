package com.example.koe.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koe.R
import com.example.koe.match.Candidate
import com.example.koe.ui.components.KoeAdaptiveFrame

/** A screen of the nav graph drawn in Compose. */
abstract class KoeComposeFragment : Fragment() {

    @Composable
    protected abstract fun ScreenContent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            // Tablets get the phone layout scaled up instead of the same sizes lost in a bigger screen.
            KoeAdaptiveFrame(background = colorResource(R.color.koe_screen_home_container)) { ScreenContent() }
        }
    }

    protected fun back() {
        findNavController().popBackStack()
    }
}

class TitleFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() = TitleScreen(onStart = { findNavController().navigate(R.id.action_title_to_menu) })
}

class MenuFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() = MainMenuScreen(
        onPlay = { findNavController().navigate(R.id.action_menu_to_character) },
        onRules = { findNavController().navigate(R.id.action_global_rules) },
        onSettings = { findNavController().navigate(R.id.action_global_settings) },
    )
}

class RulesFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() = RulesScreen(onBack = ::back)
}

class SettingsFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() = SettingsScreen(
        onBack = ::back,
        onRules = { findNavController().navigate(R.id.action_global_rules) },
    )
}

class CharacterFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() {
        var selected by rememberSaveable { mutableStateOf(Candidate.MACRON) }
        CharacterScreen(
            selected = selected,
            onSelect = { selected = it },
            onValidate = {
                findNavController().navigate(R.id.action_character_to_ready, bundleOf("candidate" to selected.name))
            },
        )
    }
}

class ReadyFragment : KoeComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val human = Candidate.valueOf(requireArguments().getString("candidate") ?: Candidate.MACRON.name)
        // Drawn once per visit and kept across rotation and process death.
        val order by rememberSaveable { mutableStateOf(Candidate.entries.shuffled()) }
        ReadyScreen(
            order = order,
            human = human,
            onBack = ::back,
            onStart = {
                findNavController().navigate(
                    R.id.action_ready_to_match,
                    bundleOf("candidate" to human.name, "order" to order.map { it.name }.toTypedArray()),
                )
            },
        )
    }
}
