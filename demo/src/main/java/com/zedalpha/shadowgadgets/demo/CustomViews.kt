package com.zedalpha.shadowgadgets.demo

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView


class InfoTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.infoTextViewStyle
) : AppCompatTextView(context, attributeSet, defStyleAttr) {
    init {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        ellipsize = TextUtils.TruncateAt.END
        gravity = Gravity.CENTER
        isClickable = true
    }

    override fun performClick(): Boolean {
        super.performClick()
        AlertDialog.Builder(context)
            .setView(R.layout.internal_info_text_view)
            .setPositiveButton("Close", null)
            .show().findViewById<TextView>(R.id.text)?.text = text
        return true
    }
}

class ZedAlphaControl @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : LinearLayout(context, attributeSet) {

    interface Listener {
        fun onElevationChange(elevation: Float)
        fun onColorChange(@ColorInt color: Int)
    }

    var listener: Listener? = null
        set(value) {
            field = value
            if (value != null) {
                callBackColor()
                callBackElevation()
            }
        }

    private val elevation by lazy { findViewById<SeekBar>(R.id.seek_elevation) }
    private val alpha by lazy { findViewById<SeekBar>(R.id.seek_alpha) }
    private val red by lazy { findViewById<SeekBar>(R.id.seek_red) }
    private val green by lazy { findViewById<SeekBar>(R.id.seek_green) }
    private val blue by lazy { findViewById<SeekBar>(R.id.seek_blue) }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        clipChildren = false
        clipToPadding = false

        inflate(context, R.layout.internal_zed_alpha_control, this)

        val array = context.obtainStyledAttributes(attributeSet, R.styleable.ZedAlphaControl)
        val exampleElevation = array.getInt(R.styleable.ZedAlphaControl_zac_elevation, 0)
        val exampleColor = array.getColor(R.styleable.ZedAlphaControl_zac_color, 0)
        array.recycle()

        elevation.progress = exampleElevation.coerceIn(0..100)
        alpha.progress = Color.alpha(exampleColor)
        red.progress = Color.red(exampleColor)
        green.progress = Color.green(exampleColor)
        blue.progress = Color.blue(exampleColor)

        elevation.setOnSeekBarChangeListener(
            object : SeekChangeListener() {
                override fun onChange(progress: Int) {
                    callBackElevation()
                }
            }
        )

        val rgbaListener = object : SeekChangeListener() {
            override fun onChange(progress: Int) {
                callBackColor()
            }
        }
        alpha.setOnSeekBarChangeListener(rgbaListener)
        red.setOnSeekBarChangeListener(rgbaListener)
        green.setOnSeekBarChangeListener(rgbaListener)
        blue.setOnSeekBarChangeListener(rgbaListener)
    }

    private fun callBackColor() {
        listener!!.onColorChange(
            Color.argb(
                alpha.progress,
                red.progress,
                green.progress,
                blue.progress
            )
        )
    }

    private fun callBackElevation() {
        listener!!.onElevationChange(elevation.progress.toFloat())
    }
}

internal abstract class SeekChangeListener : SeekBar.OnSeekBarChangeListener {
    abstract fun onChange(progress: Int)

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        onChange(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}