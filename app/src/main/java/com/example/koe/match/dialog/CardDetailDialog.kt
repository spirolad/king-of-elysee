package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogCardDetailBinding

class CardDetailDialog : KoeDialogFragment<DialogCardDetailBinding>() {

    override val widthFraction = 0.76f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogCardDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOk.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "CardDetailDialog"
    }
}
