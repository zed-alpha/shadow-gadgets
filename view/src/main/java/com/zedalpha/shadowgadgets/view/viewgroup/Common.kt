package com.zedalpha.shadowgadgets.view.viewgroup

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.ClippedShadowAttributes
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.extractShadowAttributes
import com.zedalpha.shadowgadgets.view.shadow.overlayController
import kotlin.properties.Delegates


sealed interface ClippedShadowsViewGroup {
    var clipAllChildShadows: Boolean?
    var childClippedShadowsPlane: ClippedShadowPlane?
}


internal class RegularShadowManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?
) : ViewGroupShadowManager(parentView, attributeSet) {

    private val generatedAttributes =
        mutableMapOf<Int, ClippedShadowAttributes>()

    private var attached = false

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        if (attached) return
        val attributes =
            attributeSet.extractShadowAttributes(parentView.context)
        if (attributes.id != View.NO_ID) {
            generatedAttributes += attributes.id to attributes
        }
    }

    fun onAttachedToWindow() {
        attached = true
        generatedAttributes.clear()
    }

    override fun onViewAdded(child: View) {
        if (!attached) {
            val attributes = generatedAttributes.remove(child.id)

            val plane =
                attributes?.clippedShadowPlane ?: childClippedShadowsPlane
            plane?.let { child.clippedShadowPlane = it }

            val clip = attributes?.clipOutlineShadow ?: clipAllChildShadows
            clip?.let { child.clipOutlineShadow = it }
        }
    }
}


internal class RecyclingShadowManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?
) : ViewGroupShadowManager(parentView, attributeSet),
    View.OnAttachStateChangeListener {

    init {
        parentView.addOnAttachStateChangeListener(this)
    }

    override fun onViewDetachedFromWindow(v: View) {
        parentView.overlayController?.notifyRecyclerDetach()
    }

    override fun onViewAdded(child: View) {
        if (!child.isRecyclingViewGroupChild) {
            child.isRecyclingViewGroupChild = true
            childClippedShadowsPlane?.let { child.clippedShadowPlane = it }
            child.clipOutlineShadow = true
        }
    }

    override fun onViewAttachedToWindow(v: View) {}
}

internal var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.recycling_view_group_child) == true
    private set(value) = setTag(R.id.recycling_view_group_child, value)

internal sealed class ViewGroupShadowManager(
    protected val parentView: ViewGroup,
    attributeSet: AttributeSet?
) {
    var clipAllChildShadows: Boolean? by verifyUnattached()

    var childClippedShadowsPlane: ClippedShadowPlane? by verifyUnattached()

    init {
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
            childClippedShadowsPlane =
                ClippedShadowPlane.forValue(
                    array.getInt(
                        R.styleable.ClippedShadowsViewGroup_childClippedShadowsPlane,
                        0
                    )
                )
        }
        array.recycle()
    }

    abstract fun onViewAdded(child: View)

    private fun <T> verifyUnattached() =
        Delegates.vetoable(null as T?) { _, _, _ ->
            !parentView.isAttachedToWindow
        }
}