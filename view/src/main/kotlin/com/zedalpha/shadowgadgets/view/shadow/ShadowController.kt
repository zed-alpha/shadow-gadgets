package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ExpandableListView
import android.widget.GridView
import android.widget.ListView
import android.widget.StackView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.core.layer.LocationTracker

internal abstract class ShadowController(protected val parentView: ViewGroup) {

    protected val shadows = mutableMapOf<View, GroupShadow>()

    protected val isRecyclingViewGroup = parentView.isRecyclingViewGroup

    private val attachListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                addPreDrawListener()
                onParentViewAttached()
            }

            override fun onViewDetachedFromWindow(v: View) {
                removePreDrawListener()
                if (isRecyclingViewGroup) detachAllShadows()
            }
        }

    protected open fun onParentViewAttached() {}

    private val tracker = LocationTracker(parentView)

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (requiresTracking() && tracker.checkLocationChanged()) {
            onLocationChanged()
            parentView.invalidate()
        } else {
            checkInvalidate()
        }
        true
    }

    protected abstract fun requiresTracking(): Boolean

    protected abstract fun onLocationChanged()

    protected abstract fun checkInvalidate()

    init {
        parentView.addOnAttachStateChangeListener(attachListener)
        if (parentView.isAttachedToWindow) addPreDrawListener()
    }

    @CallSuper
    protected open fun detachFromParent() {
        parentView.removeOnAttachStateChangeListener(attachListener)
        removePreDrawListener()
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun addPreDrawListener() {
        viewTreeObserver = parentView.viewTreeObserver.also { observer ->
            observer.addOnPreDrawListener(preDrawListener)
        }
        tracker.initialize()
    }

    private fun removePreDrawListener() {
        val observer = viewTreeObserver ?: return
        if (observer.isAlive) observer.removeOnPreDrawListener(preDrawListener)
        viewTreeObserver = null
    }

    fun createShadow(target: View) {
        shadows[target] = GroupShadow(target, providePlane(target))
    }

    protected abstract fun providePlane(target: View): DrawPlane

    fun disposeShadow(shadow: GroupShadow) {
        if (shadows.values.remove(shadow) && shadows.isEmpty()) onEmpty()
    }

    protected open fun onEmpty() {}

    fun detachAllShadows() {
        // Must copy because detachFromTarget() modifies shadows
        shadows.values.toList().forEach { it.detachFromTarget() }
    }
}

internal inline val ViewGroup.isRecyclingViewGroup: Boolean
    get() = javaClass.let { groupClass ->
        when {
            RecyclerView::class.java.isAssignableFrom(groupClass) -> true
            ListView::class.java.isAssignableFrom(groupClass) -> true
            GridView::class.java.isAssignableFrom(groupClass) -> true
            ExpandableListView::class.java.isAssignableFrom(groupClass) -> true
            StackView::class.java.isAssignableFrom(groupClass) -> true
            else -> false
        }
    }