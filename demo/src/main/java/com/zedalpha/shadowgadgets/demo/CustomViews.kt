package com.zedalpha.shadowgadgets.demo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView


class ZedAlphaControl @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : RelativeLayout(context, attributeSet) {

    interface Listener {
        fun onElevationChange(exampleView: View, elevation: Float)
        fun onColorChange(exampleView: View, @ColorInt color: Int)
    }

    var listener: Listener? = null
        set(value) {
            field = value
            if (value != null) {
                callBackColor()
                callBackElevation()
            }
        }

    private val exampleView by lazy { findViewById<View>(R.id.view_example) }
    private val elevation by lazy { findViewById<SeekBar>(R.id.seek_elevation) }
    private val alpha by lazy { findViewById<SeekBar>(R.id.seek_alpha) }
    private val red by lazy { findViewById<SeekBar>(R.id.seek_red) }
    private val green by lazy { findViewById<SeekBar>(R.id.seek_green) }
    private val blue by lazy { findViewById<SeekBar>(R.id.seek_blue) }

    init {
        clipChildren = false
        clipToPadding = false
        ignoreGravity = R.id.view_example
        gravity = Gravity.CENTER_VERTICAL

        inflate(context, R.layout.internal_zed_alpha_control, this)

        val array = context.obtainStyledAttributes(attributeSet, R.styleable.ZedAlphaControl)
        val exampleElevation = array.getInt(R.styleable.ZedAlphaControl_exampleElevation, 0)
        val exampleColor = array.getColor(R.styleable.ZedAlphaControl_exampleColor, 0)
        val exampleTint = array.getColor(R.styleable.ZedAlphaControl_exampleTint, 0)
        array.recycle()

        elevation.progress = exampleElevation.coerceIn(0..100)
        alpha.progress = Color.alpha(exampleColor)
        red.progress = Color.red(exampleColor)
        green.progress = Color.green(exampleColor)
        blue.progress = Color.blue(exampleColor)

        if (exampleTint != 0) {
            exampleView.backgroundTintList = ColorStateList.valueOf(exampleTint)
        }

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
        listener?.onColorChange(
            exampleView,
            Color.argb(alpha.progress, red.progress, green.progress, blue.progress)
        )
    }

    private fun callBackElevation() {
        listener?.onElevationChange(exampleView, elevation.progress.toFloat())
    }
}

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
            .setMessage(text)
            .show()
        return true
    }
}