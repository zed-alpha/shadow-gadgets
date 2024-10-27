package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.res.TypedArray
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.disableZ
import com.zedalpha.shadowgadgets.core.enableZ
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.shadow.DrawPlane
import com.zedalpha.shadowgadgets.view.shadow.GroupShadow
import com.zedalpha.shadowgadgets.view.shadow.ShadowController
import kotlin.properties.Delegates

internal abstract class ShadowsViewGroupManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    private val attachViewToParent: (View, Int, LayoutParams) -> Unit,
    private val detachAllViewsFromParent: () -> Unit,
    private val superDispatchDraw: (Canvas) -> Unit,
    private val superDrawChild: (Canvas, View, Long) -> Boolean
) : ShadowController(parentView), DrawPlane {

    protected var groupPlaneSet = false
    var childShadowsPlane by initOnly(Foreground) { groupPlaneSet = true }

    protected var groupClipSet = false
    var clipAllChildShadows by initOnly(false) { groupClipSet = true }

    protected var groupColorSet = false
    var childOutlineShadowsColorCompat
            by initOnly(DefaultShadowColorInt) { groupColorSet = true }

    protected var groupForceSet = false
    var forceChildOutlineShadowsColorCompat
            by initOnly(false) { groupForceSet = true }

    protected inline val View.planeNotSet
        get() = getTag(R.id.shadow_plane) == null

    protected inline val View.clipNotSet
        get() = getTag(R.id.clip_outline_shadow) == null

    protected inline val View.colorNotSet
        get() = getTag(R.id.outline_shadow_color_compat) == null

    protected inline val View.forceNotSet
        get() = getTag(R.id.force_outline_shadow_color_compat) == null

    var ignoreInlineChildShadows by initOnly(isRecyclingViewGroup) {}

    protected var attached = false
        private set

    private fun <T> initOnly(initial: T, onSet: () -> Unit) =
        Delegates.vetoable(initial) { _, _, _ ->
            (!attached).also { if (it) onSet() }
        }

    init {
        @Suppress("LeakingThis")
        parentView.inlineController = this

        val array = parentView.context.obtainStyledAttributes(
            attributeSet,
            R.styleable.ShadowsViewGroup
        )
        parentView.saveDebugData(attributeSet, array)

        if (array.hasValue(R.styleable.ShadowsViewGroup_childShadowsPlane)) {
            childShadowsPlane = ShadowPlane.forValue(
                array.getInt(
                    R.styleable.ShadowsViewGroup_childShadowsPlane,
                    Foreground.ordinal
                )
            )
        }
        if (array.hasValue(R.styleable.ShadowsViewGroup_clipAllChildShadows)) {
            clipAllChildShadows = array.getBoolean(
                R.styleable.ShadowsViewGroup_clipAllChildShadows,
                false
            )
        }
        if (array.hasValue(R.styleable.ShadowsViewGroup_ignoreInlineChildShadows)) {
            ignoreInlineChildShadows = array.getBoolean(
                R.styleable.ShadowsViewGroup_ignoreInlineChildShadows,
                false
            )
        }
        array.recycle()

        parentView.addOnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
            layers.setSize(r - l, b - t)
        }
    }

    override fun onParentViewAttached() {
        attached = true
    }

    abstract fun onViewAdded(child: View)

    override fun providePlane(target: View) = this

    private val layers = InlineLayerSet(parentView)

    override fun addShadow(shadow: GroupShadow, color: Int) {
        layers.addShadow(shadow, color)
        parentView.invalidate()
    }

    override fun removeShadow(shadow: GroupShadow) {
        layers.removeShadow(shadow)
        disposeShadow(shadow)
        parentView.invalidate()
    }

    override fun updateColor(shadow: GroupShadow, color: Int) {
        layers.updateColor(shadow, color)
        parentView.invalidate()
    }

    override fun invalidatePlane() = parentView.invalidate()

    override fun dispose() = layers.dispose()

    override fun requiresTracking() = layers.requiresTracking()

    override fun onLocationChanged() = layers.recreate()

    override fun checkInvalidate() = layers.refresh()

    private var unsorted = arrayOfNulls<View>(ARRAY_INITIAL_CAPACITY)

    private var sorted = arrayOfNulls<View>(ARRAY_INITIAL_CAPACITY)

    fun dispatchDraw(canvas: Canvas) {
        if (shadows.isEmpty()) {
            superDispatchDraw(canvas)
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
            superDispatchDraw(canvas)
            reorderChildren(unsorted, childCount)
        }
    }

    private fun reorderChildren(ordered: Array<View?>, childCount: Int) {
        detachAllViewsFromParent()
        val attach = attachViewToParent
        for (index in 0 until childCount) {
            val view = ordered[index]!!
            attach(view, index, view.layoutParams)
            ordered[index] = null
        }
    }

    fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val shadow = shadows[child]
        if (shadow == null || !canvas.isHardwareAccelerated) {
            return superDrawChild(canvas, child, drawingTime)
        }

        disableZ(canvas)
        val result: Boolean
        if (shadow.isClipped) {
            result = superDrawChild(canvas, child, drawingTime)
            layers.draw(canvas, shadow)
        } else {
            layers.draw(canvas, shadow)
            result = superDrawChild(canvas, child, drawingTime)
        }
        enableZ(canvas)

        return result
    }
}

internal inline var ViewGroup.inlineController: ShadowController
    get() = getTag(R.id.inline_controller) as ShadowController
    private set(value) = setTag(R.id.inline_controller, value)

// Mirroring ViewGroup's mChildren capacity

private const val ARRAY_INITIAL_CAPACITY = 12

private const val ARRAY_CAPACITY_INCREMENT = 12

private fun nextSize(childCount: Int) =
    (childCount / ARRAY_CAPACITY_INCREMENT + 1) * ARRAY_CAPACITY_INCREMENT

private val UnsafeZComparator =
    Comparator<View?> { v1, v2 -> v1!!.z.compareTo(v2!!.z) }

private fun View.saveDebugData(attrs: AttributeSet?, array: TypedArray) {
    if (Build.VERSION.SDK_INT < 29) return
    saveAttributeDataForStyleable(
        context, R.styleable.ShadowsViewGroup, attrs, array, 0, 0
    )
}