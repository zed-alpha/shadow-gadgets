package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.core.view.doOnAttach

internal fun interface OnPreDraw : ViewTreeObserver.OnPreDrawListener {

    @Deprecated("Override invoke()")
    @CallSuper
    override fun onPreDraw(): Boolean {
        invoke()
        return true
    }

    operator fun invoke()
}

// Mirroring addOnDraw(), just in case.
internal fun View.addOnPreDraw(action: OnPreDraw) =
    if (this.isAttachedToWindow) {
        this.viewTreeObserver.addOnPreDrawListener(action)
    } else {
        doOnAttach { it.viewTreeObserver.addOnPreDrawListener(action) }
    }

internal fun View.removeOnPreDraw(action: OnPreDraw) =
    this.viewTreeObserver.removeOnPreDrawListener(action)


internal fun interface OnDraw : ViewTreeObserver.OnDrawListener {

    @Deprecated("Override invoke()")
    @CallSuper
    override fun onDraw() = invoke()

    operator fun invoke()
}

// Apparently older versions mishandle OnDrawListeners if the View is
// unattached, even though they should be merged upon attach.
internal fun View.addOnDraw(action: OnDraw) =
    if (this.isAttachedToWindow) {
        this.viewTreeObserver.addOnDrawListener(action)
    } else {
        doOnAttach { it.viewTreeObserver.addOnDrawListener(action) }
    }

internal fun View.removeOnDraw(action: OnDraw) =
    this.viewTreeObserver.removeOnDrawListener(action)