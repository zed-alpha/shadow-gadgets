package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.doOnAttach

internal fun interface OnPreDraw : ViewTreeObserver.OnPreDrawListener {

    override fun onPreDraw(): Boolean {
        preDraw()
        return true
    }

    fun preDraw()
}

// Mirroring addOnDraw(), just in case.
internal fun View.addOnPreDraw(action: OnPreDraw) =
    doOnAttach { it.viewTreeObserver.addOnPreDrawListener(action) }

internal fun View.removeOnPreDraw(action: OnPreDraw) =
    this.viewTreeObserver.removeOnPreDrawListener(action)


internal fun interface OnDraw : ViewTreeObserver.OnDrawListener

// Apparently older versions lose OnDrawListeners if they're added before
// the View is attached, even though they should be merged once that happens.
internal fun View.addOnDraw(action: OnDraw) =
    doOnAttach { it.viewTreeObserver.addOnDrawListener(action) }

internal fun View.removeOnDraw(action: OnDraw) =
    this.viewTreeObserver.removeOnDrawListener(action)