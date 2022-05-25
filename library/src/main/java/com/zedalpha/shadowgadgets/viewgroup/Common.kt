@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.*
import com.zedalpha.shadowgadgets.shadow.shadowController
import kotlin.properties.Delegates


sealed interface ClippedShadowsViewGroup {
    val isUsingShadowsFallback: Boolean
    var clipAllChildShadows: Boolean?
    var childClippedShadowsPlane: ClippedShadowPlane?
    var childShadowsFallbackStrategy: ShadowFallbackStrategy?
}


internal class RegularShadowManager(parentView: ViewGroup, attributeSet: AttributeSet?) :
    ViewGroupShadowManager(parentView, attributeSet) {

    private val generatedAttributes = mutableMapOf<Int, ClippedShadowAttributes>()

    private var attached = false

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        if (attached) return
        val attributes = attributeSet.extractShadowAttributes(parentView.context)
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

            val plane = attributes?.clippedShadowPlane ?: childClippedShadowsPlane
            plane?.let { child.clippedShadowPlane = it }

            val strategy = attributes?.shadowFallbackStrategy ?: childShadowsFallbackStrategy
            strategy?.let { child.shadowFallbackStrategy = it }

            val clip = attributes?.clipOutlineShadow ?: clipAllChildShadows
            clip?.let { child.clipOutlineShadow = it }
        }
    }
}

internal class RecyclingShadowManager(parentView: ViewGroup, attributeSet: AttributeSet?) :
    ViewGroupShadowManager(parentView, attributeSet) {

    override fun onViewAdded(child: View) {
        if (!child.isRecyclingViewGroupChild) {
            child.isRecyclingViewGroupChild = true
            childClippedShadowsPlane?.let { child.clippedShadowPlane = it }
            childShadowsFallbackStrategy?.let { child.shadowFallbackStrategy = it }
            child.clipOutlineShadow = true
        }
    }
}

internal sealed class ViewGroupShadowManager(
    protected val parentView: ViewGroup,
    attributeSet: AttributeSet?
) {
    val isUsingShadowsFallback: Boolean
        get() = parentView.shadowController?.isUsingFallbackMethod
            ?: parentView.shouldUseFallbackMethod

    var clipAllChildShadows: Boolean? by verifyUnattached()

    var childClippedShadowsPlane: ClippedShadowPlane? by verifyUnattached()

    var childShadowsFallbackStrategy: ShadowFallbackStrategy? by verifyUnattached()

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
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_childShadowsFallbackStrategy)) {
            childShadowsFallbackStrategy =
                ShadowFallbackStrategy.forValue(
                    array.getInt(
                        R.styleable.ClippedShadowsViewGroup_childShadowsFallbackStrategy,
                        0
                    )
                )
        }
        if (array.hasValue(R.styleable.ClippedShadowsViewGroup_forceShadowsFallbackMethod)) {
            parentView.forceShadowsFallbackMethod =
                array.getBoolean(
                    R.styleable.ClippedShadowsViewGroup_forceShadowsFallbackMethod,
                    false
                )
        }
        array.recycle()
    }

    abstract fun onViewAdded(child: View)

    private fun <T> verifyUnattached() =
        Delegates.vetoable(null as T?) { _, _, _ -> !parentView.isAttachedToWindow }
}