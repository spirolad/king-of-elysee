package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import com.example.koe.R
import com.example.koe.databinding.DialogMechanceteBinding

class MechanceteDialog : KoeDialogFragment<DialogMechanceteBinding>() {

    override val widthFraction: Float = 0.81f

    @get:StyleRes
    override val themeRes: Int = R.style.ThemeOverlay_Koe_Dialog_Evil

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogMechanceteBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.btnValidate.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "MechanceteDialog"
    }
}
