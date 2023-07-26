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
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.shadow.GroupShadow
import com.zedalpha.shadowgadgets.view.shadow.ShadowController
import kotlin.properties.Delegates


internal abstract class ClippedShadowsViewGroupManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    private val detachAllViewsFromParent: () -> Unit,
    private val attachViewToParent: (View, Int, LayoutParams) -> Unit
) : ShadowController(parentView) {

    var clipAllChildShadows: Boolean? by verifyUnattached()

    var childClippedShadowsPlane: ClippedShadowPlane? by verifyUnattached()

    protected var unattached = true

    init {
        @Suppress("LeakingThis")
        parentView.inlineController = this

        val array = parentView.context.obtainStyledAttributes(
            attributeSet,
            R.styleable.ClippedShadowsViewGroup
        )
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_clipAllChildShadows)) {
            clipAllChildShadows =
                array.getBoolean(
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
        array.recycle()
    }

    @CallSuper
    override fun onParentViewAttached() {
        unattached = false
    }

    private fun <T> verifyUnattached() =
        Delegates.vetoable(null as T?) { _, _, _ -> unattached }

    abstract fun onViewAdded(child: View)

    override fun createShadow(target: View) = GroupShadow(target, this, null)

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