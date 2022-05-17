@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.ChipGroup
import com.zedalpha.shadowgadgets.R


class ClippedShadowsChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.chipGroupStyle
) : ChipGroup(context, attrs, defStyleAttr), ClippedShadowsViewGroup {
    private val manager = RegularShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
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


class ClippedShadowsConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
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


class ClippedShadowsCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), ClippedShadowsViewGroup {
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


class ClippedShadowsMaterialButtonToggleGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonToggleGroupStyle
) : MaterialButtonToggleGroup(context, attrs, defStyleAttr), ClippedShadowsViewGroup {
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


class ClippedShadowsMotionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), ClippedShadowsViewGroup {
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