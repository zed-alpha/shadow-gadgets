@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper
import com.zedalpha.shadowgadgets.shadow.RenderNodeShadow
import com.zedalpha.shadowgadgets.shadow.Shadow
import com.zedalpha.shadowgadgets.shadow.ViewShadow
import com.zedalpha.shadowgadgets.shadow.ViewShadowContainer
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup.ShadowPlane


internal sealed interface ViewGroupShadow

internal sealed interface ViewGroupContainer<T> where T : Shadow, T : ViewGroupShadow {
    fun createShadow(child: View, @ShadowPlane clippedShadowPlane: Int?): T

    fun updateAndInvalidateShadows()

    fun wrapDispatchDraw(canvas: Canvas, dispatchDraw: () -> Unit)

    fun onSizeChanged(w: Int, h: Int)
}


internal class ViewGroupViewShadow(
    targetView: View,
    container: ViewShadowContainer
) : ViewShadow(targetView, container), ViewGroupShadow

@SuppressLint("ViewConstructor")
internal class ViewGroupViewShadowContainer(viewGroup: ViewGroup) :
    ViewGroupContainer<ViewGroupViewShadow> {

    private val container by lazy { ViewShadowContainer(viewGroup).apply { attach() } }

    override fun createShadow(child: View, clippedShadowPlane: Int?) =
        ViewGroupViewShadow(child, container)

    override fun updateAndInvalidateShadows() {
        container.updateAndInvalidateShadows()
    }

    override fun wrapDispatchDraw(canvas: Canvas, dispatchDraw: () -> Unit) {
        dispatchDraw()
    }

    override fun onSizeChanged(w: Int, h: Int) {
        /* No-op. */
    }
}


internal class ViewGroupRenderNodeShadow(
    targetView: View,
    private val container: ViewGroupRenderNodeShadowContainer,
    @ShadowPlane clippedShadowPlane: Int = ClippedShadowsViewGroup.FOREGROUND
) : RenderNodeShadow(targetView), ViewGroupShadow {
    @ShadowPlane
    var clippedShadowPlane: Int = clippedShadowPlane
        set(value) {
            if (field != value) {
                container.remove(this)
                field = value
                container.add(this)
                container.invalidate()
            }
        }

    override fun attach() {
        super.attach()
        container.add(this)
    }

    override fun detach() {
        super.detach()
        container.remove(this)
    }
}


internal class ViewGroupRenderNodeShadowContainer(private val viewGroup: ViewGroup) :
    ViewGroupContainer<ViewGroupRenderNodeShadow> {

    private val foregroundShadows = mutableListOf<ViewGroupRenderNodeShadow>()
    private val backgroundShadows = mutableListOf<ViewGroupRenderNodeShadow>()

    private val receiver: RenderNodeWrapper by lazy {
        RenderNodeFactory.newInstance().also { it.setProjectionReceiver(true) }
    }

    private val projector: RenderNodeWrapper by lazy {
        RenderNodeFactory.newInstance().also { it.setProjectBackwards(true) }
    }

    fun add(shadow: ViewGroupRenderNodeShadow) {
        if (shadow.clippedShadowPlane == ClippedShadowsViewGroup.FOREGROUND) {
            foregroundShadows += shadow
        } else {
            backgroundShadows += shadow
        }
    }

    fun remove(shadow: ViewGroupRenderNodeShadow) {
        if (shadow.clippedShadowPlane == ClippedShadowsViewGroup.FOREGROUND) {
            foregroundShadows -= shadow
        } else {
            backgroundShadows -= shadow
        }
    }

    fun invalidate() {
        viewGroup.invalidate()
    }

    override fun createShadow(child: View, clippedShadowPlane: Int?) =
        ViewGroupRenderNodeShadow(
            child,
            this,
            clippedShadowPlane ?: ClippedShadowsViewGroup.FOREGROUND
        )

    override fun updateAndInvalidateShadows() {
        var invalidate = false
        backgroundShadows.forEach { if (it.update()) invalidate = true }
        foregroundShadows.forEach { if (it.update()) invalidate = true }
        if (invalidate) viewGroup.invalidate()
    }

    override fun wrapDispatchDraw(canvas: Canvas, dispatchDraw: () -> Unit) {
        val drawBackground = backgroundShadows.isNotEmpty()
        if (drawBackground) receiver.draw(canvas)

        dispatchDraw()

        if (drawBackground) {
            val shadowCanvas = projector.beginRecording(viewGroup.width, viewGroup.height)
            backgroundShadows.forEach { it.draw(shadowCanvas) }
            projector.endRecording(shadowCanvas)
            projector.draw(canvas)
        }

        foregroundShadows.forEach { it.draw(canvas) }
    }

    override fun onSizeChanged(w: Int, h: Int) {
        receiver.setPosition(0, 0, w, h)
        projector.setPosition(0, 0, w, h)
    }
}