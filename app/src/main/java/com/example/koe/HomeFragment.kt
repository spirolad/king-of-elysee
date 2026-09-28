package com.example.koe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koe.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private data class CharacterModel(
        val id: String,
        val name: String,
        val quote: String,
        val imageResId: Int,
        val thumbnailContainer: FrameLayout,
    )

    private var selectedCharacterIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val characters = listOf(
            CharacterModel(
                id = "macron",
                name = getString(R.string.character_macron_name),
                quote = getString(R.string.character_macron_quote),
                imageResId = R.drawable.macron,
                thumbnailContainer = binding.flThumbMacron,
            ),
            CharacterModel(
                id = "lepen",
                name = getString(R.string.character_lepen_name),
                quote = getString(R.string.character_lepen_quote),
                imageResId = R.drawable.lepen,
                thumbnailContainer = binding.flThumbLepen,
            ),
            CharacterModel(
                id = "melenchon",
                name = getString(R.string.character_melenchon_name),
                quote = getString(R.string.character_melenchon_quote),
                imageResId = R.drawable.melanchon,
                thumbnailContainer = binding.flThumbMelenchon,
            ),
            CharacterModel(
                id = "poutou",
                name = getString(R.string.character_poutou_name),
                quote = getString(R.string.character_poutou_quote),
                imageResId = R.drawable.poutou,
                thumbnailContainer = binding.flThumbPoutou,
            ),
        )

        fun updateSelection(index: Int) {
            selectedCharacterIndex = index
            val character = characters[index]

            binding.tvCharacterName.text = character.name
            binding.tvCharacterQuote.text = character.quote
            binding.ivCharacterImage.setImageResource(character.imageResId)

            characters.forEachIndexed { i, item ->
                if (i == index) {
                    item.thumbnailContainer.setBackgroundResource(R.drawable.bg_thumbnail_selected)
                } else {
                    item.thumbnailContainer.setBackgroundResource(R.drawable.bg_thumbnail_unselected)
                }
            }
        }

        characters.forEachIndexed { index, item ->
            item.thumbnailContainer.setOnClickListener {
                updateSelection(index)
            }
        }

        updateSelection(0)

        binding.btnStartGame.setOnClickListener {
            val selectedName = characters[selectedCharacterIndex].name
            val bundle = Bundle().apply {
                putString("characterName", selectedName)
            }
            findNavController().navigate(R.id.action_homeFragment_to_gameFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}