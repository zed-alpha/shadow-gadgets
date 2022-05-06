@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadow.*
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup.ShadowPlane
import kotlin.properties.Delegates


internal sealed class ViewGroupManager(
    private val viewGroup: ViewGroup,
    attrs: AttributeSet?
) {
    val isUsingFallback: Boolean = !RenderNodeFactory.isOpenForBusiness

    var clipAllChildShadows: Boolean by verifyUnattached(false)

    var disableChildShadowsOnFallback: Boolean by verifyUnattached(false)

    @ShadowPlane
    var childClippedShadowPlane: Int by verifyUnattached(ClippedShadowsViewGroup.FOREGROUND)

    init {
        val array =
            viewGroup.context.obtainStyledAttributes(attrs, R.styleable.ClippedShadowsViewGroup)
        clipAllChildShadows =
            array.getBoolean(R.styleable.ClippedShadowsViewGroup_clipAllChildShadows, false)
        disableChildShadowsOnFallback = array.getBoolean(
            R.styleable.ClippedShadowsViewGroup_disableChildShadowsOnFallback,
            false
        )
        childClippedShadowPlane = array.getInt(
            R.styleable.ClippedShadowsViewGroup_childClippedShadowPlane,
            ClippedShadowsViewGroup.FOREGROUND
        )
        array.recycle()
    }

    private val container: ViewGroupContainer<*> = if (isUsingFallback) {
        ViewGroupViewShadowContainer(viewGroup)
    } else {
        ViewGroupRenderNodeShadowContainer(viewGroup)
    }

    private val onDrawListener = ViewTreeObserver.OnDrawListener {
        container.updateAndInvalidateShadows()
    }

    fun onAttachedToWindow() {
        viewGroup.viewTreeObserver.addOnDrawListener(onDrawListener)
    }

    fun onDetachedFromWindow() {
        viewGroup.viewTreeObserver.removeOnDrawListener(onDrawListener)
    }

    fun onSizeChanged(w: Int, h: Int) {
        container.onSizeChanged(w, h)
    }

    fun wrapDispatchDraw(canvas: Canvas, dispatchDraw: () -> Unit) {
        container.wrapDispatchDraw(canvas, dispatchDraw)
    }

    fun setChildClipOutlineShadow(
        child: View,
        clipOutlineShadow: Boolean,
        disableShadowOnFallback: Boolean,
        @ShadowPlane clippedShadowPlane: Int
    ) {
        if (child.parent != viewGroup || child.clipOutlineShadow) return

        val shadow = child.shadow
        val isClipped = shadow != null
        if (clipOutlineShadow != isClipped) {
            if (!clipOutlineShadow) {
                shadow!!.detach()
            } else {
                clipOrDisableChildShadow(child, disableShadowOnFallback, clippedShadowPlane)
            }
        } else {
            if (isClipped) {
                if (disableShadowOnFallback && isUsingFallback) {
                    shadow!!.detach()
                    wrapOutlineProvider(child)
                }
                if (shadow is ViewGroupRenderNodeShadow) {
                    shadow.clippedShadowPlane = clippedShadowPlane
                }
            }
        }
    }

    abstract fun onViewAdded(child: View)
    abstract fun onViewRemoved(child: View)

    protected fun clipOrDisableChildShadow(
        child: View,
        disableShadowOnFallback: Boolean? = null,
        @ShadowPlane clippedShadowPlane: Int? = null
    ) {
        if (isUsingFallback && disableShadowOnFallback ?: child.disableShadowOnFallback) {
            wrapOutlineProvider(child)
        } else {
            container.createShadow(child, clippedShadowPlane).attach()
        }
    }

    private fun <T> verifyUnattached(initialValue: T) =
        Delegates.vetoable(initialValue) { _, _, _ ->
            if (viewGroup.isAttachedToWindow) {
                throw IllegalStateException("Must be set before View is attached to Window.")
            }
            true
        }

    private val View.disableShadowOnFallback
        get() = disableChildShadowsOnFallback ||
                clippedShadowsLayoutParams?.disableShadowOnFallback == true

    private fun wrapOutlineProvider(child: View) {
        child.outlineProvider = ZeroAlphaProviderWrapper(child.outlineProvider)
    }
}

internal class RegularManager(
    viewGroup: ViewGroup,
    attrs: AttributeSet?
) : ViewGroupManager(viewGroup, attrs) {
    override fun onViewAdded(child: View) {
        if (shouldClipChildShadow(child) && child.outlineProvider != null) {
            val params = child.layoutParams as? ClippedShadowsLayoutParams
            clipOrDisableChildShadow(
                child,
                params?.disableShadowOnFallback,
                params?.clippedShadowPlane
            )
        }
    }

    override fun onViewRemoved(child: View) {
        val shadow = child.shadow
        if (shadow != null) {
            shadow.detach()
        } else {
            val provider = child.outlineProvider
            if (provider is ZeroAlphaProviderWrapper) {
                child.outlineProvider = provider.wrapped
            }
        }
    }

    private fun shouldClipChildShadow(child: View) =
        child.clippedShadowsLayoutParams?.clipOutlineShadow == true ||
                clipAllChildShadows || child.isTaggedForClipping

    private val View.isTaggedForClipping: Boolean
        get() = clipText.let { it == tag || it == getTag(R.id.clip_outline_shadow_tag_id) }

    private val clipText by lazy {
        viewGroup.resources.getText(R.string.clip_outline_shadow_tag_value)
    }
}

internal class RecyclingManager(
    viewGroup: ViewGroup,
    attrs: AttributeSet?
) : ViewGroupManager(viewGroup, attrs) {
    override fun onViewAdded(child: View) {
        val shadow = child.shadow
        if (shadow == null) {
            val provider = child.outlineProvider
            if (provider != null && provider !is ZeroAlphaProviderWrapper) {
                clipOrDisableChildShadow(child)
            }
        } else {
            shadow.show()
        }
    }

    override fun onViewRemoved(child: View) {
        child.shadow?.hide()
    }
}

private class ZeroAlphaProviderWrapper(
    val wrapped: ViewOutlineProvider
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        outline.alpha = 0.0F
    }
}