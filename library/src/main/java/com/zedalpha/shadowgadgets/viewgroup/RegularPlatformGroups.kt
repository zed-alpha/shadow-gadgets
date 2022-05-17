@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi


class ClippedShadowsFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RegularShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        manager.onAttachedToWindow()
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowsLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RegularShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        manager.onAttachedToWindow()
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RadioGroup(context, attrs), ClippedShadowsViewGroup {
    private val manager = RegularShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        manager.onAttachedToWindow()
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowsRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RegularShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        manager.onAttachedToWindow()
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}