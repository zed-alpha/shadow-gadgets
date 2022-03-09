@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.getClipAllChildShadows
import com.zedalpha.shadowgadgets.getDisableShadowsOnFallback
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadow.*


interface ClippedShadowsContainer {
    val isUsingShadowsFallback: Boolean
}

internal sealed class ContainerManager(
    private val viewGroup: ViewGroup,
    attrs: AttributeSet?
) {
    val isUsingFallback = true //!RenderNodeFactory.isOpenForBusiness

    private val disableShadowsOnFallback: Boolean =
        viewGroup.context.getDisableShadowsOnFallback(attrs)

    protected val viewShadows by lazy { mutableListOf<ViewShadow>() }

    protected val viewShadowContainer by lazy {
        val group = viewGroup
        val container = ViewShadowContainer(viewGroup.context, viewShadows)
        if (group.isLaidOut) container.layout(0, 0, group.width, group.height)
        group.addOnLayoutChangeListener(container)
        group.overlay.add(container)
        container
    }

    protected fun setOrDisableChildShadow(child: View) {
        child.inlineShadow =
            when {
                !isUsingFallback -> RenderNodeShadow(child)
                !disableShadowsOnFallback -> {
                    val shadow = ViewInlineShadow(child, viewShadowContainer)
                    viewShadowContainer.addView(shadow.shadowView)
                    viewShadows += shadow
                    shadow
                }
                else -> {
                    child.outlineProvider = ZeroAlphaProviderWrapper(child.outlineProvider)
                    null
                }
            }
        child.inlineShadow?.attachToTargetView()
    }

    fun drawChild(canvas: Canvas, child: View) {
        if (viewGroup.isHardwareAccelerated) (child.inlineShadow as? RenderNodeShadow)?.draw(canvas)
    }
}

internal class RecyclerContainerManager(
    viewGroup: ViewGroup,
    attrs: AttributeSet?
) : ContainerManager(viewGroup, attrs) {
    init {
        viewGroup.viewTreeObserver.addOnPreDrawListener {
            viewGroup.children.forEach { child ->
                val shadow = child.inlineShadow
                if (shadow != null && shadow.updateShadow()) {
                    if (shadow is RenderNodeShadow) {
                        child.invalidate()
                    } else if (shadow is ViewShadow) {
                        shadow.shadowView.invalidate()
                    }
                }
            }
            true
        }
    }

    fun onViewAdded(child: View) {
        val inlineShadow = child.inlineShadow
        if (inlineShadow == null) {
            val provider = child.outlineProvider
            if (provider != null && provider !is ZeroAlphaProviderWrapper) {
                setOrDisableChildShadow(child)
            }
        } else if (inlineShadow is ViewInlineShadow) {
            viewShadowContainer.addView(inlineShadow.shadowView)
            viewShadows += inlineShadow
        }
    }

    fun onViewRemoved(child: View) {
        val shadow = child.inlineShadow
        if (shadow is ViewInlineShadow) {
            viewShadowContainer.removeView(shadow.shadowView)
            viewShadows -= shadow
        }
    }
}

internal class StandardContainerManager(
    viewGroup: ViewGroup,
    attrs: AttributeSet?
) : ContainerManager(viewGroup, attrs) {
    private val clipAllChildShadows: Boolean = viewGroup.context.getClipAllChildShadows(attrs)

    fun onViewAdded(child: View, clipAttribute: Boolean) {
        if ((clipAllChildShadows || clipAttribute || child.isTaggedForClipping)
            && child.outlineProvider != null
        ) {
            setOrDisableChildShadow(child)
        }
    }

    fun onViewRemoved(child: View) {
        val shadow = child.inlineShadow
        if (shadow != null) {
            if (shadow is ViewInlineShadow) {
                viewShadowContainer.removeView(shadow.shadowView)
                viewShadows -= shadow
            }
            shadow.detachFromTargetView()
            child.inlineShadow = null
        } else {
            val provider = child.outlineProvider
            if (provider is ZeroAlphaProviderWrapper) {
                child.outlineProvider = provider.wrapped
            }
        }
    }
}


internal class ZeroAlphaProviderWrapper(
    val wrapped: ViewOutlineProvider
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        outline.alpha = 0.0F
    }
}


internal var View.inlineShadow: Shadow?
    get() = getTag(R.id.tag_target_inline_shadow) as? Shadow
    set(value) {
        setTag(R.id.tag_target_inline_shadow, value)
    }


private var clipText: CharSequence? = null

internal val View.isTaggedForClipping: Boolean
    get() {
        if (clipText == null) clipText = resources.getText(R.string.clip_outline_shadow_tag_value)
        return clipText.let { it == tag || it == getTag(R.id.clip_outline_shadow_tag_id) }
    }