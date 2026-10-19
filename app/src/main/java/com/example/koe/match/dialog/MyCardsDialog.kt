package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogMyCardsBinding

class MyCardsDialog : KoeDialogFragment<DialogMyCardsBinding>() {

    override val widthFraction = 0.89f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogMyCardsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener { dismiss() }
        binding.card1.root.setOnLongClickListener {
            CardDetailDialog().show(childFragmentManager, CardDetailDialog.TAG)
            true
        }
        binding.card2.root.setOnLongClickListener {
            CardDetailDialog().show(childFragmentManager, CardDetailDialog.TAG)
            true
        }
        binding.card3.root.setOnLongClickListener {
            CardDetailDialog().show(childFragmentManager, CardDetailDialog.TAG)
            true
        }
    }

    companion object {
        const val TAG = "MyCardsDialog"
    }
}
