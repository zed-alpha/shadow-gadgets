package com.zedalpha.shadowgadgets.demo

import android.widget.SeekBar


abstract class SeekChangeListener : SeekBar.OnSeekBarChangeListener {
    abstract fun onChange(progress: Int)

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        onChange(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}