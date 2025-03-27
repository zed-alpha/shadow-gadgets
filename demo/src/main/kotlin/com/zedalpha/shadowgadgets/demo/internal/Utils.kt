package com.zedalpha.shadowgadgets.demo.internal

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.zedalpha.shadowgadgets.demo.R

internal const val DefaultTargetColor = 0x7F547FA8

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
    val hideWelcome = preferences.getBoolean(PREF_HIDE_WELCOME, false)
    if (hideWelcome) return

    AlertDialog.Builder(this)
        .setView(R.layout.dialog_welcome)
        .setPositiveButton("Close", null)
        .show()
        .findViewById<CheckBox>(R.id.hide_welcome)
        ?.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit { putBoolean(PREF_HIDE_WELCOME, isChecked) }
        }
}

private const val PREF_HIDE_WELCOME = "hide_welcome"

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