package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogYieldElyseeBinding

class YieldElyseeDialog : KoeDialogFragment<DialogYieldElyseeBinding>() {

    override val widthFraction: Float = 0.66f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogYieldElyseeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.btnStay.setOnClickListener { dismiss() }
        binding.btnYield.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "YieldElyseeDialog"
    }
}
