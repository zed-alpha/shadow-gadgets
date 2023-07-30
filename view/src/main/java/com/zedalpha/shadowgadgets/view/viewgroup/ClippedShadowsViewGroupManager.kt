package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.disableZ
import com.zedalpha.shadowgadgets.core.enableZ
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.shadow.GroupShadow
import com.zedalpha.shadowgadgets.view.shadow.ShadowController
import com.zedalpha.shadowgadgets.view.shadow.ShadowPlane
import kotlin.properties.Delegates


internal abstract class ClippedShadowsViewGroupManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    private val detachAllViewsFromParent: () -> Unit,
    private val attachViewToParent: (View, Int, LayoutParams) -> Unit
) : ShadowController(parentView), ShadowPlane {

    protected var clipSet = false
    var clipAllChildShadows: Boolean by initOnly(false) { clipSet = true }

    protected var planeSet = false
    var childClippedShadowsPlane: ClippedShadowPlane by initOnly(Foreground) {
        planeSet = true
    }

    var ignoreInlineChildShadows: Boolean by initOnly(false) {}

    protected var unattached = true

    init {
        @Suppress("LeakingThis")
        parentView.inlineController = this

        val array = parentView.context.obtainStyledAttributes(
            attributeSet,
            R.styleable.ClippedShadowsViewGroup
        )
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_clipAllChildShadows)) {
            clipAllChildShadows = array.getBoolean(
                R.styleable.ClippedShadowsViewGroup_clipAllChildShadows,
                false
            )
        }
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_childClippedShadowsPlane)) {
            childClippedShadowsPlane = ClippedShadowPlane.forValue(
                array.getInt(
                    R.styleable.ClippedShadowsViewGroup_childClippedShadowsPlane,
                    0
                )
            )
        }
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_ignoreInlineChildShadows)) {
            ignoreInlineChildShadows = array.getBoolean(
                R.styleable.ClippedShadowsViewGroup_ignoreInlineChildShadows,
                false
            )
        }
        array.recycle()
    }

    @CallSuper
    override fun onParentViewAttached() {
        unattached = false
    }

    private fun <T> initOnly(initial: T, onSet: () -> Unit) =
        Delegates.vetoable(initial) { _, _, _ ->
            unattached.also { if (it) onSet() }
        }

    abstract fun onViewAdded(child: View)

    override fun createShadow(target: View) = GroupShadow(target, this, this)

    override fun invalidatePlane() {
        parentView.invalidate()
    }

    override fun onPreDraw() {
        shadows.values.forEach { shadow ->
            if (shadow.checkInvalidate()) {
                parentView.invalidate()
                return
            }
        }
    }

    private var unsorted = arrayOfNulls<View>(ARRAY_INITIAL_CAPACITY)

    private var sorted = arrayOfNulls<View>(ARRAY_INITIAL_CAPACITY)

    fun dispatchDraw(superDispatchDraw: () -> Unit) {
        if (shadows.isEmpty()) {
            superDispatchDraw()
        } else {
            val childCount = parentView.childCount

            if (unsorted.size < childCount) {
                val size = nextSize(childCount)
                unsorted = arrayOfNulls(size)
                sorted = arrayOfNulls(size)
            }

            for (index in 0 until childCount) {
                val child = parentView.getChildAt(index)
                unsorted[index] = child
                sorted[index] = child
            }
            sorted.sortWith(UnsafeZComparator, 0, childCount)

            reorderChildren(sorted, childCount)
            superDispatchDraw()
            reorderChildren(unsorted, childCount)
        }
    }

    private fun reorderChildren(childArray: Array<View?>, childCount: Int) {
        detachAllViewsFromParent()
        for (index in 0 until childCount) {
            val view = childArray[index]!!
            attachViewToParent(view, index, view.layoutParams)
            childArray[index] = null
        }
    }

    fun drawChild(canvas: Canvas, child: View) {
        if (!canvas.isHardwareAccelerated) return
        val shadow = shadows[child] ?: return
        disableZ(canvas)
        shadow.draw(canvas)
        enableZ(canvas)
    }
}

internal var ViewGroup.inlineController: ShadowController
    get() = getTag(R.id.inline_controller) as ShadowController
    private set(value) = setTag(R.id.inline_controller, value)


// Mirroring ViewGroup's mChildren capacity

private const val ARRAY_INITIAL_CAPACITY = 12

private const val ARRAY_CAPACITY_INCREMENT = 12

private fun nextSize(childCount: Int) =
    (childCount / ARRAY_CAPACITY_INCREMENT + 1) * ARRAY_CAPACITY_INCREMENT

private val UnsafeZComparator =
    Comparator<View?> { v1, v2 -> v1!!.z.compareTo(v2!!.z) }