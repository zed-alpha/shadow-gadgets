@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.ClippedShadowPlane
import com.zedalpha.shadowgadgets.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.isRecyclingViewGroupChild
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadow.shadowController
import kotlin.properties.Delegates


sealed interface ClippedShadowsViewGroup {
    val isUsingShadowsFallback: Boolean
    var clipAllChildShadows: Boolean
    var childClippedShadowsPlane: ClippedShadowPlane
    var disableChildShadowsOnFallback: Boolean
}

internal sealed interface ClippedShadowsLayoutParams {
    var clipOutlineShadow: Boolean?
    var clippedShadowPlane: ClippedShadowPlane?
    var disableShadowOnFallback: Boolean?
}

internal val View.clippedShadowsLayoutParams: ClippedShadowsLayoutParams?
    get() = layoutParams as? ClippedShadowsLayoutParams

internal fun AttributeSet?.extractClippedShadowsLayoutParamsValues(
    context: Context,
    params: ClippedShadowsLayoutParams
) {
    val array = context.obtainStyledAttributes(this, R.styleable.ClippedShadowsLayoutParams)
    if (array.hasValue(R.styleable.ClippedShadowsLayoutParams_clipOutlineShadow)) {
        params.clipOutlineShadow =
            array.getBoolean(R.styleable.ClippedShadowsLayoutParams_clipOutlineShadow, false)
    }
    if (array.hasValue(R.styleable.ClippedShadowsLayoutParams_clippedShadowPlane)) {
        params.clippedShadowPlane = ClippedShadowPlane.forValue(
            array.getInt(R.styleable.ClippedShadowsLayoutParams_clippedShadowPlane, 0)
        )
    }
    if (array.hasValue(R.styleable.ClippedShadowsLayoutParams_disableShadowOnFallback)) {
        params.disableShadowOnFallback =
            array.getBoolean(R.styleable.ClippedShadowsLayoutParams_disableShadowOnFallback, false)
    }
    array.recycle()
}

internal class ViewGroupShadowManager(
    private val parentView: ViewGroup,
    attributeSet: AttributeSet?
) {
    val isUsingFallback: Boolean = !RenderNodeFactory.isOpenForBusiness

    var clipAllChildShadows: Boolean by refreshable(false)

    var childClippedShadowsPlane: ClippedShadowPlane by refreshable(Foreground)

    var disableChildShadowsOnFallback: Boolean by refreshable(false)

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
        disableChildShadowsOnFallback = array.getBoolean(
            R.styleable.ClippedShadowsViewGroup_disableChildShadowsOnFallback,
            false
        )
        array.recycle()
    }

    fun onViewAdded(child: View, recyclingViewGroup: Boolean = false) {
        if (recyclingViewGroup) {
            if (!child.isRecyclingViewGroupChild) {
                child.isRecyclingViewGroupChild = true
                child.clipOutlineShadow = true
            }
        } else {
            val shadowsParent = child.parent as? ClippedShadowsViewGroup
            child.clipOutlineShadow = (child.clippedShadowsLayoutParams?.clipOutlineShadow
                ?: shadowsParent?.clipAllChildShadows) == true || child.isTaggedForClipping
        }
    }

    private fun <T> refreshable(initialValue: T) =
        Delegates.observable(initialValue) { _, oldValue, newValue ->
            if (oldValue != newValue) parentView.shadowController?.refreshAll()
        }

    private val View.isTaggedForClipping: Boolean
        get() = clipText.let { it == tag || it == getTag(R.id.clip_outline_shadow_tag_id) }

    private val clipText by lazy {
        parentView.resources.getText(R.string.clip_outline_shadow_tag_value)
    }
}