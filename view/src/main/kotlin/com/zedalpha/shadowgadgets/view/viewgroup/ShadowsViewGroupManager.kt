package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.annotation.CallSuper
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.doOnAttach
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.disableZ
import com.zedalpha.shadowgadgets.view.internal.enableZ
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.plane.inlinePlane
import com.zedalpha.shadowgadgets.view.proxy.isRecycling
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import kotlin.properties.Delegates

internal abstract class ShadowsViewGroupManager<T>(
    protected val viewGroup: T,
    attributeSet: AttributeSet?,
    private val attachViewToParent: (View, Int, LayoutParams) -> Unit,
    private val detachAllViewsFromParent: () -> Unit,
    private val superDispatchDraw: (Canvas) -> Unit,
    private val superDrawChild: (Canvas, View, Long) -> Boolean
) where T : ViewGroup, T : ShadowsViewGroup {

    protected var isGroupPlaneSet = false
        private set

    protected var isGroupClipSet = false
        private set

    protected var isGroupColorSet = false
        private set

    protected var isGroupForceSet = false
        private set

    var childShadowsPlane: ShadowPlane
            by initOnly(Foreground) { isGroupPlaneSet = true }

    var clipAllChildShadows: Boolean
            by initOnly(false) { isGroupClipSet = true }

    var childOutlineShadowsColorCompat: Int
            by initOnly(DefaultShadowColor) { isGroupColorSet = true }

    var forceChildOutlineShadowsColorCompat: Boolean
            by initOnly(false) { isGroupForceSet = true }

    var takeOverDrawForInlineChildShadows: Boolean
            by initOnly(!viewGroup.isRecycling) {}

    var ignoreInlineChildShadows: Boolean
        get() = !takeOverDrawForInlineChildShadows
        set(value) {
            takeOverDrawForInlineChildShadows = !value
        }

    private fun <T> initOnly(initial: T, onSet: () -> Unit) =
        Delegates.vetoable(initial) { _, _, _ ->
            (!isAttached).also { unattached -> if (unattached) onSet() }
        }

    protected var isAttached = false
        private set

    init {
        viewGroup.context.withStyledAttributes(
            set = attributeSet,
            attrs = R.styleable.ShadowsViewGroup
        ) {
            ViewCompat.saveAttributeDataForStyleable(
                /* view = */ viewGroup,
                /* context = */ viewGroup.context,
                /* styleable = */ R.styleable.ShadowsViewGroup,
                /* attrs = */  attributeSet,
                /* t = */ this,
                /* defStyleAttr = */  0,
                /* defStyleRes = */  0
            )

            if (hasValue(R.styleable.ShadowsViewGroup_childShadowsPlane)) {
                val value =
                    getInt(
                        /* index = */ R.styleable.ShadowsViewGroup_childShadowsPlane,
                        /* defValue = */ childShadowsPlane.ordinal
                    )
                childShadowsPlane = ShadowPlane.entries[value]
            }
            if (hasValue(R.styleable.ShadowsViewGroup_clipAllChildShadows)) {
                clipAllChildShadows =
                    getBoolean(
                        /* index = */ R.styleable.ShadowsViewGroup_clipAllChildShadows,
                        /* defValue = */ clipAllChildShadows
                    )
            }
            if (hasValue(R.styleable.ShadowsViewGroup_ignoreInlineChildShadows)) {
                ignoreInlineChildShadows =
                    getBoolean(
                        /* index = */ R.styleable.ShadowsViewGroup_ignoreInlineChildShadows,
                        /* defValue = */ ignoreInlineChildShadows
                    )
            }
            if (hasValue(R.styleable.ShadowsViewGroup_takeOverDrawForInlineChildShadows)) {
                takeOverDrawForInlineChildShadows =
                    getBoolean(
                        /* index = */ R.styleable.ShadowsViewGroup_takeOverDrawForInlineChildShadows,
                        /* defValue = */ takeOverDrawForInlineChildShadows
                    )
            }
        }

        viewGroup.doOnAttach { onAttach() }
    }

    @CallSuper
    protected open fun onAttach() {
        isAttached = true
    }

    abstract fun onViewAdded(child: View)

    private var unsorted = arrayOfNulls<View>(ChildArrayInitialCapacity)
    private var sorted = arrayOfNulls<View>(ChildArrayInitialCapacity)

    private var inlinePlane: Plane? = null

    fun dispatchDraw(canvas: Canvas) {
        val inlinePlane =
            viewGroup.inlinePlane
                .takeIf { canvas.isHardwareAccelerated }
                .also { this.inlinePlane = it }

        if (inlinePlane == null) {
            superDispatchDraw(canvas)
        } else {
            val childCount = viewGroup.childCount

            if (unsorted.size < childCount) {
                val size = nextSize(childCount)
                unsorted = arrayOfNulls(size)
                sorted = arrayOfNulls(size)
            }

            for (index in 0..<childCount) {
                val child = viewGroup.getChildAt(index)
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
        for (index in 0..<childCount) {
            val view = ordered[index]!!
            attach(view, index, view.layoutParams)
            ordered[index] = null
        }
    }

    fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val proxy = child.shadowProxy?.takeIf { it.plane === inlinePlane }
        val result: Boolean

        if (proxy != null && proxy.updateAndConfirmDraw()) {
            disableZ(canvas)
            proxy.layer?.draw(canvas) ?: proxy.shadow.draw(canvas)
            result = superDrawChild(canvas, child, drawingTime)
            enableZ(canvas)
        } else {
            result = superDrawChild(canvas, child, drawingTime)
        }

        return result
    }
}

internal val View.isPlaneNotSet: Boolean
    get() = this.getTag(R.id.shadow_plane) == null

internal val View.isClipNotSet: Boolean
    get() = this.getTag(R.id.clip_outline_shadow) == null

internal val View.isColorNotSet: Boolean
    get() = this.getTag(R.id.outline_shadow_color_compat) == null

internal val View.isForceNotSet: Boolean
    get() = this.getTag(R.id.force_outline_shadow_color_compat) == null

// Mirroring ViewGroup's mChildren capacity
private const val ChildArrayInitialCapacity = 12
private const val ChildArrayCapacityIncrement = 12

private fun nextSize(childCount: Int) =
    (childCount / ChildArrayCapacityIncrement + 1) * ChildArrayCapacityIncrement

private val UnsafeZComparator =
    Comparator<View?> { v1, v2 -> v1!!.z.compareTo(v2!!.z) }