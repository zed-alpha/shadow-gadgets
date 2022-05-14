@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.*
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


sealed interface ClippedShadowsViewGroup {
    val isUsingShadowsFallback: Boolean
    val clipAllChildShadows: Boolean
    val childClippedShadowsPlane: ClippedShadowPlane
    val childShadowsFallbackStrategy: ShadowFallbackStrategy
}


internal class ViewGroupShadowManager<T>(
    private val parentView: T,
    attributeSet: AttributeSet?,
    private val isRecyclingViewGroup: Boolean = false
) where T : ViewGroup, T : ClippedShadowsViewGroup {
    val isUsingFallback: Boolean = !RenderNodeFactory.isOpenForBusiness

    val clipAllChildShadows: Boolean

    val childClippedShadowsPlane: ClippedShadowPlane

    val childShadowsFallbackStrategy: ShadowFallbackStrategy

    init {
        val array = parentView.context.obtainStyledAttributes(
            attributeSet,
            R.styleable.ClippedShadowsViewGroup
        )
        clipAllChildShadows = array.getBoolean(
            R.styleable.ClippedShadowsViewGroup_clipAllChildShadows,
            false
        )
        childClippedShadowsPlane = ClippedShadowPlane.forValue(
            array.getInt(R.styleable.ClippedShadowsViewGroup_childClippedShadowsPlane, 0)
        )
        childShadowsFallbackStrategy = ShadowFallbackStrategy.forValue(
            array.getInt(R.styleable.ClippedShadowsViewGroup_childShadowsFallbackStrategy, 0)
        )
        array.recycle()
    }

    private val generatedAttributes = mutableMapOf<Int, ClippedShadowAttributes>()

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        val attributes = attributeSet.extractShadowAttributes(parentView.context)
        if (attributes != null && attributes.id != View.NO_ID) {
            generatedAttributes += attributes.id to attributes
        }
    }

    fun onViewAdded(child: View) {
        if (isRecyclingViewGroup) {
            if (!child.isRecyclingViewGroupChild) {
                child.isRecyclingViewGroupChild = true
                child.clipOutlineShadow = true
            }
        } else {
            val parent = parentView as ClippedShadowsViewGroup
            val attributes = generatedAttributes.remove(child.id)
            if (!child.isClippedShadowPlaneExplicitlySet) {
                child.clippedShadowPlane =
                    attributes?.clippedShadowPlane ?: parent.childClippedShadowsPlane
            }
            if (!child.isShadowFallbackStrategyExplicitlySet) {
                child.shadowFallbackStrategy =
                    attributes?.shadowFallbackStrategy ?: parent.childShadowsFallbackStrategy
            }
            if (!child.isClipOutlineShadowExplicitlySet) {
                child.clipOutlineShadow =
                    attributes?.clipOutlineShadow ?: parent.clipAllChildShadows
            }
        }
    }
}


private data class ClippedShadowAttributes(
    val id: Int,
    val clipOutlineShadow: Boolean? = null,
    val clippedShadowPlane: ClippedShadowPlane? = null,
    val shadowFallbackStrategy: ShadowFallbackStrategy? = null
)


private fun AttributeSet?.extractShadowAttributes(context: Context): ClippedShadowAttributes? {
    val array = context.obtainStyledAttributes(this, R.styleable.ClippedShadowAttributes)
    return if (array.hasValue(R.styleable.ClippedShadowAttributes_android_id)) {
        ClippedShadowAttributes(
            array.getResourceId(R.styleable.ClippedShadowAttributes_android_id, View.NO_ID),
            if (array.hasValue(R.styleable.ClippedShadowAttributes_clipOutlineShadow)) {
                array.getBoolean(R.styleable.ClippedShadowAttributes_clipOutlineShadow, false)
            } else null,
            if (array.hasValue(R.styleable.ClippedShadowAttributes_clippedShadowPlane)) {
                ClippedShadowPlane.forValue(
                    array.getInt(R.styleable.ClippedShadowAttributes_clippedShadowPlane, 0)
                )
            } else null,
            if (array.hasValue(R.styleable.ClippedShadowAttributes_shadowFallbackStrategy)) {
                ShadowFallbackStrategy.forValue(
                    array.getInt(R.styleable.ClippedShadowAttributes_shadowFallbackStrategy, 0)
                )
            } else null
        )
    } else {
        null
    }.also { array.recycle() }
}


private val View.isClipOutlineShadowExplicitlySet: Boolean
    get() = getTag(R.id.tag_target_clip_outline_shadow) is Boolean

private val View.isClippedShadowPlaneExplicitlySet: Boolean
    get() = getTag(R.id.tag_target_clipped_shadow_plane) is ClippedShadowPlane

private val View.isShadowFallbackStrategyExplicitlySet: Boolean
    get() = getTag(R.id.tag_target_shadow_fallback_strategy) is Boolean