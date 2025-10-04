package com.zedalpha.shadowgadgets.demo.internal

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.zedalpha.shadowgadgets.demo.R

internal const val DefaultTargetColor = 0x7f547fa8

internal fun View.applyInsetsListener() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = bars.left
            topMargin = bars.top
            rightMargin = bars.right
            bottomMargin = bars.bottom
        }
        insets
    }
}

internal fun Activity.showWelcomeDialog() {
    val preferences = getPreferences(Context.MODE_PRIVATE)
    val hideWelcome = preferences.getBoolean(PrefHideWelcome, false)
    if (hideWelcome) return

    AlertDialog.Builder(this)
        .setView(R.layout.dialog_welcome)
        .setPositiveButton("Close", null)
        .show()
        .findViewById<CheckBox>(R.id.hide_welcome)
        ?.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit { putBoolean(PrefHideWelcome, isChecked) }
        }
}

private const val PrefHideWelcome = "hide_welcome"

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

@Suppress("NOTHING_TO_INLINE")
internal inline fun @receiver:ColorInt Int.toColorStateList(): ColorStateList =
    ColorStateList.valueOf(this)

internal class RoundedCornerViewOutlineProvider(val radiusDp: Float = 5F) :
    ViewOutlineProvider() {

    private var radius: Float? = null

    override fun getOutline(view: View, outline: Outline) {
        val radius = radius
            ?: (radiusDp * view.context.resources.displayMetrics.density)
                .also { radius = it }
        outline.setRoundRect(0, 0, view.width, view.height, radius)
    }
}