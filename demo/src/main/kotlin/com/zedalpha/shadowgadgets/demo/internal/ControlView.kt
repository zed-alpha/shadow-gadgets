package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.zedalpha.shadowgadgets.demo.databinding.InternalControlViewBinding

class ControlView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val ui =
        InternalControlViewBinding.inflate(LayoutInflater.from(context), this)

    var color: Int
        get() = Color.argb(
            ui.seekAlpha.progress,
            ui.seekRed.progress,
            ui.seekGreen.progress,
            ui.seekBlue.progress
        )
        set(value) {
            ui.seekAlpha.progress = Color.alpha(value)
            ui.seekRed.progress = Color.red(value)
            ui.seekGreen.progress = Color.green(value)
            ui.seekBlue.progress = Color.blue(value)
        }

    private var onColorChanged: (Int) -> Unit = {}

    fun onColorChanged(callback: (Int) -> Unit) {
        onColorChanged = callback
    }

    fun syncColor() = onColorChanged(color)

    var elevation: Int
        get() = ui.seekElevation.progress
        set(value) {
            ui.seekElevation.progress = value
        }

    private var onElevationChanged: (Int) -> Unit = {}

    fun onElevationChanged(callback: (Int) -> Unit) {
        onElevationChanged = callback
    }

    fun syncElevation() = onElevationChanged(elevation)

    init {
        val seekListener = SeekChangeListener { onColorChanged(color) }
        ui.seekAlpha.setOnSeekBarChangeListener(seekListener)
        ui.seekRed.setOnSeekBarChangeListener(seekListener)
        ui.seekGreen.setOnSeekBarChangeListener(seekListener)
        ui.seekBlue.setOnSeekBarChangeListener(seekListener)

        ui.seekElevation.setOnSeekBarChangeListener(
            SeekChangeListener { onElevationChanged(it) }
        )
    }
}