package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper

internal abstract class ShadowController(protected val parentView: ViewGroup) {

    protected val shadows = mutableMapOf<View, GroupShadow>()

    private val attachListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                addPreDrawListener()
                onParentViewAttached()
            }

            override fun onViewDetachedFromWindow(v: View) {
                removePreDrawListener()
                onParentViewDetached()
            }
        }

    protected open fun onParentViewAttached() {}

    protected open fun onParentViewDetached() {}

    private val preDrawListener =
        ViewTreeObserver.OnPreDrawListener { onPreDraw(); true }

    protected abstract fun onPreDraw()

    init {
        parentView.addOnAttachStateChangeListener(attachListener)
        if (parentView.isAttachedToWindow) addPreDrawListener()
    }

    @CallSuper
    protected open fun detachFromParent() {
        parentView.removeOnAttachStateChangeListener(attachListener)
        if (parentView.isAttachedToWindow) removePreDrawListener()
    }

    private fun addPreDrawListener() {
        parentView.viewTreeObserver.addOnPreDrawListener(preDrawListener)
    }

    private fun removePreDrawListener() {
        parentView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
    }

    fun createGroupShadow(target: View) {
        shadows[target] = createShadow(target)
    }

    protected abstract fun createShadow(target: View): GroupShadow

    fun removeShadow(shadow: GroupShadow) {
        shadows.values -= shadow
        if (shadows.isEmpty()) onEmpty()
    }

    protected open fun onEmpty() {}

    fun detachAllShadows() {
        // Must copy because detachFromTarget() modifies shadows
        shadows.values.toList().forEach { it.detachFromTarget() }
    }
}