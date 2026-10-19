package com.example.koe.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.koe.R
import com.example.koe.databinding.FragmentMatchBinding
import com.example.koe.match.dialog.MechanceteDialog
import com.example.koe.match.dialog.MyCardsDialog
import com.example.koe.match.dialog.PauseDialog
import com.example.koe.match.dialog.PlayerDetailsDialog
import com.example.koe.match.dialog.ShopDialog
import com.example.koe.match.dialog.YieldElyseeDialog

class MatchFragment : Fragment() {

    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The board edge is 47dp in total; MainActivity already pads the parent by the cutout.
        val boardEdge = resources.getDimensionPixelSize(R.dimen.koe_board_edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            root.updatePadding(
                left = (boardEdge - cutout.left).coerceAtLeast(0),
                right = (boardEdge - cutout.right).coerceAtLeast(0),
            )
            insets
        }

        binding.btnPause.setOnClickListener {
            PauseDialog().show(childFragmentManager, PauseDialog.TAG)
        }

        val openPlayerDetails = {
            PlayerDetailsDialog().show(childFragmentManager, PlayerDetailsDialog.TAG)
        }
        binding.chipMelenchon.root.setOnClickListener { openPlayerDetails() }
        binding.chipLepen.root.setOnClickListener { openPlayerDetails() }
        binding.chipPoutou.root.setOnClickListener { openPlayerDetails() }

        binding.btnShop.setOnClickListener {
            ShopDialog().show(childFragmentManager, ShopDialog.TAG)
        }
        binding.btnCards.setOnClickListener {
            MyCardsDialog().show(childFragmentManager, MyCardsDialog.TAG)
        }
        binding.btnValidate.setOnClickListener {
            YieldElyseeDialog().show(childFragmentManager, YieldElyseeDialog.TAG)
        }
        binding.btnRoll.setOnClickListener {
            MechanceteDialog().show(childFragmentManager, MechanceteDialog.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
