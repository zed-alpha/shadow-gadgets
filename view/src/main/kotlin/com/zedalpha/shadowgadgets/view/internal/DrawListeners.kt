package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import android.view.ViewTreeObserver

// Apparently older Android versions lose OnDrawListeners added before the
// View is attached, even though they should be merged once that happens.
// We handle OnPreDrawListeners in the same manner, just in case.

// These used to be functional interfaces; keeping usage the same.
internal class OnPreDraw(action: () -> Unit) :
    ViewTreeObserver.OnPreDrawListener, AutoAttachDrawListener(action) {

    override fun addDrawListener(vto: ViewTreeObserver) =
        vto.addOnPreDrawListener(this)

    override fun removeDrawListener(vto: ViewTreeObserver) =
        vto.removeOnPreDrawListener(this)

    override fun onPreDraw(): Boolean {
        action.invoke()
        return true
    }
}

internal fun View.addOnPreDraw(action: OnPreDraw) = this.add(action)

internal fun View.removeOnPreDraw(action: OnPreDraw) = this.remove(action)


internal class OnDraw(action: () -> Unit) :
    ViewTreeObserver.OnDrawListener, AutoAttachDrawListener(action) {

    override fun addDrawListener(vto: ViewTreeObserver) =
        vto.addOnDrawListener(this)

    override fun removeDrawListener(vto: ViewTreeObserver) =
        vto.removeOnDrawListener(this)

    override fun onDraw() = action.invoke()
}

internal fun View.addOnDraw(action: OnDraw) = this.add(action)

internal fun View.removeOnDraw(action: OnDraw) = this.remove(action)


internal abstract class AutoAttachDrawListener(protected val action: () -> Unit) :
    View.OnAttachStateChangeListener {

    abstract fun addDrawListener(vto: ViewTreeObserver)

    abstract fun removeDrawListener(vto: ViewTreeObserver)

    final override fun onViewAttachedToWindow(v: View) {
        addDrawListener(v.viewTreeObserver)
        v.removeOnAttachStateChangeListener(this)
    }

    // Detach is handled externally and doesn't depend on View state.
    final override fun onViewDetachedFromWindow(v: View) {}
}

private fun View.add(listener: AutoAttachDrawListener) =
    if (this.isAttachedToWindow) {
        listener.addDrawListener(this.viewTreeObserver)
    } else {
        this.addOnAttachStateChangeListener(listener)
    }

private fun View.remove(listener: AutoAttachDrawListener) =
    if (this.isAttachedToWindow) {
        listener.removeDrawListener(this.viewTreeObserver)
    } else {
        this.removeOnAttachStateChangeListener(listener)
    }