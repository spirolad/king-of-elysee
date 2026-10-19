package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.koe.R
import com.example.koe.databinding.DialogQuitConfirmBinding

class QuitConfirmDialog : KoeDialogFragment<DialogQuitConfirmBinding>() {

    override val widthFraction: Float = 0.55f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogQuitConfirmBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnQuit.setOnClickListener {
            dismiss()
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    companion object {
        const val TAG = "QuitConfirmDialog"
    }
}
