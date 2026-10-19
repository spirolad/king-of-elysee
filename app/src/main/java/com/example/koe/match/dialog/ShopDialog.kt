package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogShopBinding

class ShopDialog : KoeDialogFragment<DialogShopBinding>() {

    override val widthFraction = 0.89f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogShopBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnBuy.setOnClickListener { dismiss() }
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
        const val TAG = "ShopDialog"
    }
}
