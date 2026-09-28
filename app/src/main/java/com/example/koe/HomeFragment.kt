package com.example.koe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.koe.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        binding.btnStartGame.setOnClickListener {
            val selectedCharacter = when (binding.rgCharacters.checkedRadioButtonId) {
                R.id.rb_macron -> "Macron"
                R.id.rb_lepen -> "Le Pen"
                R.id.rb_poutou -> "Poutou"
                else -> "Macron"
            }

            val bundle = Bundle().apply {
                putString("characterName", selectedCharacter)
            }
            findNavController().navigate(R.id.action_homeFragment_to_gameFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}