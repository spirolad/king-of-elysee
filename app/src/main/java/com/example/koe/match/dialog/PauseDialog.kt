package com.example.koe.match.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.koe.databinding.DialogPauseBinding

class PauseDialog : KoeDialogFragment<DialogPauseBinding>() {

    override val widthFraction: Float = 0.66f

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogPauseBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.btnResume.setOnClickListener { dismiss() }
        binding.btnQuit.setOnClickListener {
            QuitConfirmDialog().show(parentFragmentManager, QuitConfirmDialog.TAG)
            dismiss()
        }
    }

    companion object {
        const val TAG = "PauseDialog"
    }
}
