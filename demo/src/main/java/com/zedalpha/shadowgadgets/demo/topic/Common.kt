package com.zedalpha.shadowgadgets.demo.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.zedalpha.shadowgadgets.demo.view.ContentCardView


internal interface Topic {
    val title: String
    val descriptionResId: Int
    fun createContentFragment(): ContentFragment
}

internal abstract class ContentFragment(
    private val contentLayout: Int
) : Fragment() {

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ContentCardView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        inflater.inflate(contentLayout, this)
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadUi((view as ViewGroup).getChildAt(0))
    }

    abstract fun loadUi(view: View)
}

internal fun interface SeekChangeListener : SeekBar.OnSeekBarChangeListener {

    fun onChange(progress: Int)

    override fun onProgressChanged(
        seekBar: SeekBar,
        progress: Int,
        fromUser: Boolean
    ) {
        if (fromUser) onChange(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}

const val DefaultTargetColor = 0x7F547FA8