package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogPlayerDetailsBinding

class PlayerDetailsDialog : KoeDialogFragment<DialogPlayerDetailsBinding>() {

    override val widthFraction = 0.96f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogPlayerDetailsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "PlayerDetailsDialog"
    }
}
