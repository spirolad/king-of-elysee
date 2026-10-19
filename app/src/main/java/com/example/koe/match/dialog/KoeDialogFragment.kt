package com.example.koe.match.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.example.koe.R

/**
 * Base of every match popup: applies the Koe dialog theme, a transparent window so the
 * layout's own rounded background shows, and a width relative to the screen.
 */
abstract class KoeDialogFragment<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    /** Share of the screen width taken by the popup. */
    protected open val widthFraction: Float = 0.66f

    @get:StyleRes
    protected open val themeRes: Int = R.style.ThemeOverlay_Koe_Dialog

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, themeRes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * widthFraction).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
