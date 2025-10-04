package com.zedalpha.shadowgadgets.view.plane

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.BuildConfig
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.Tag
import com.zedalpha.shadowgadgets.view.internal.debugId
import com.zedalpha.shadowgadgets.view.internal.isRecyclingViewGroupChild
import com.zedalpha.shadowgadgets.view.internal.parentViewGroup
import com.zedalpha.shadowgadgets.view.onShadowAttach
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.shadow.Shadow
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.tintOutlineShadow
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup

internal interface Plane {
    val viewGroup: ViewGroup?
    fun addProxy(proxy: ShadowProxy)
    fun updateLayer(proxy: ShadowProxy)
    fun removeProxy(proxy: ShadowProxy)
    fun doNotMatch(shadow: Shadow, target: View): Boolean
    fun invalidate()
}

internal fun Plane.isInvalid(proxy: ShadowProxy): Boolean =
    proxy.isShown && this.doNotMatch(proxy.shadow, proxy.target)

@SuppressLint("StaticFieldLeak")
internal object NullPlane : Plane {
    override val viewGroup: ViewGroup? = null
    override fun addProxy(proxy: ShadowProxy) {}
    override fun updateLayer(proxy: ShadowProxy) {}
    override fun removeProxy(proxy: ShadowProxy) {}
    override fun doNotMatch(shadow: Shadow, target: View): Boolean = false
    override fun invalidate() {}
}

internal fun updatePlane(proxy: ShadowProxy) {
    val current = proxy.plane
    val target = proxy.target

    val parent =
        target.parentViewGroup
            ?: current?.viewGroup?.takeIf { target.isRecyclingViewGroupChild }
    val next =
        if (parent != null) {
            resolveChildPlane(current, proxy, target, parent)
        } else {
            resolveRootPlane(current, proxy, target)
        }

    proxy.plane = next ?: return
    next.invalidate()

    target.onShadowAttach?.invoke(next != NullPlane)
}

private fun resolveChildPlane(
    current: Plane?,
    proxy: ShadowProxy,
    target: View,
    parent: ViewGroup
): Plane? {
    val shadowPlane = target.shadowPlane
    return when {
        target.tintOutlineShadow && !target.clipOutlineShadow &&
                shadowPlane != ShadowPlane.Background -> {
            nullPlane(current, proxy) {
                "Color compat without the clip must use the Background plane"
            }
        }

        shadowPlane == ShadowPlane.Foreground -> {
            confirmCreate(current, { it != parent.foregroundPlane }) {
                parent.getOrCreateForegroundPlane()
            }
        }

        shadowPlane == ShadowPlane.Background -> {
            confirmCreate(current, { it != parent.backgroundPlane }) {
                parent.getOrCreateBackgroundPlane()
            }
        }

        parent is ShadowsViewGroup && !parent.ignoreInlineChildShadows -> {
            confirmCreate(current, { it != parent.inlinePlane }) {
                parent.getOrCreateInlinePlane()
            }
        }

        else -> {
            val targetClip = target.clipToOutline
            val parentClip = parent.clipChildren
            if (targetClip || parentClip) {
                nullPlane(current, proxy) {
                    buildString {
                        append("Inline shadow must not have ")
                        if (targetClip) append("target.clipToOutline=true")
                        if (targetClip && parentClip) append(" or ")
                        if (parentClip) append("parent.clipChildren=true")
                    }
                }
            } else {
                confirmCreate(current, { it !is ChildSoloPlane }) {
                    ChildSoloPlane(proxy, parent)
                }
            }
        }
    }
}

private fun resolveRootPlane(
    current: Plane?,
    proxy: ShadowProxy,
    target: View
): Plane? =
    when {
        target.shadowPlane != ShadowPlane.Inline -> {
            nullPlane(current, proxy) {
                "Library shadows on root Views must use the Inline plane"
            }
        }

        target.clipToOutline -> {
            nullPlane(current, proxy) {
                "Root inline shadow must not have target.clipToOutline=true"
            }
        }

        target.tintOutlineShadow && !target.clipOutlineShadow -> {
            nullPlane(current, proxy) {
                "Color compat on root Views requires the clip feature too"
            }
        }

        target !is ViewGroup && !RenderNodeFactory.isOpen -> {
            nullPlane(current, proxy) {
                "Fallback draw method is unavailable on non-ViewGroup roots"
            }
        }

        else -> {
            confirmCreate(current, { it !is RootSoloPlane }) {
                RootSoloPlane(proxy)
            }
        }
    }

private fun nullPlane(
    current: Plane?,
    proxy: ShadowProxy,
    message: () -> String
): Plane? =
    confirmCreate(current, { it != NullPlane }) {
        if (BuildConfig.DEBUG) {
            Log.w(Tag, "${proxy.target.debugId()}: ${message()}")
        }
        NullPlane
    }

private inline fun confirmCreate(
    current: Plane?,
    confirm: (Plane) -> Boolean,
    create: () -> Plane
): Plane? =
    if (current == null || confirm(current)) create() else null