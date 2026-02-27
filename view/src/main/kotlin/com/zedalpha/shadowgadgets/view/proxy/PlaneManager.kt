package com.zedalpha.shadowgadgets.view.proxy

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.BuildConfig
import com.zedalpha.shadowgadgets.view.ExperimentalShadowGadgets
import com.zedalpha.shadowgadgets.view.ShadowException
import com.zedalpha.shadowgadgets.view.ShadowGadgets
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.debugId
import com.zedalpha.shadowgadgets.view.internal.parentViewGroup
import com.zedalpha.shadowgadgets.view.isInShadowUpdate
import com.zedalpha.shadowgadgets.view.onShadowModeChange
import com.zedalpha.shadowgadgets.view.plane.ChildSoloPlane
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.plane.RootSoloPlane
import com.zedalpha.shadowgadgets.view.plane.backgroundPlane
import com.zedalpha.shadowgadgets.view.plane.foregroundPlane
import com.zedalpha.shadowgadgets.view.plane.getOrCreateBackgroundPlane
import com.zedalpha.shadowgadgets.view.plane.getOrCreateForegroundPlane
import com.zedalpha.shadowgadgets.view.plane.getOrCreateInlinePlane
import com.zedalpha.shadowgadgets.view.plane.inlinePlane
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.tintOutlineShadow
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup

internal fun ShadowProxy.updatePlane(): Boolean {
    val target = this.target
    if (target.isInShadowUpdate) return false

    val parent =
        if (target.isAttachedToWindow) {
            target.parentViewGroup
        } else {
            this.requireParentRecyclingViewGroup()
        }

    return if (parent != null) {
        childPlane(this, target, parent)
    } else {
        rootPlane(this, target)
    }
}

private fun childPlane(
    proxy: ShadowProxy,
    target: View,
    parent: ViewGroup
): Boolean {
    val shadowPlane = target.shadowPlane
    val current = proxy.plane

    when {
        target.tintOutlineShadow && !target.clipOutlineShadow &&
                shadowPlane != ShadowPlane.Background -> {
            handleError(proxy) {
                "Color compat shadows without the clip " +
                        "feature enabled must use the Background plane"
            }
        }

        shadowPlane == ShadowPlane.Foreground -> {
            if (current != parent.foregroundPlane) {
                proxy.plane = parent.getOrCreateForegroundPlane()
                return true
            }
        }

        shadowPlane == ShadowPlane.Background -> {
            if (current != parent.backgroundPlane) {
                proxy.plane = parent.getOrCreateBackgroundPlane()
                return true
            }
        }

        parent is ShadowsViewGroup && parent.takeOverDrawForInlineChildShadows -> {
            if (current != parent.inlinePlane) {
                proxy.plane = parent.getOrCreateInlinePlane()
                return true
            }
        }

        else -> {
            val targetClip = target.clipToOutline
            val parentClip = parent.clipChildren
            if (targetClip || parentClip) {
                handleError(proxy) {
                    buildString {
                        append("Inline shadows cannot have ")
                        if (targetClip) append("target.clipToOutline=true")
                        if (targetClip && parentClip) append(" or ")
                        if (parentClip) append("parent.clipChildren=true")
                    }
                }
            } else {
                if (current !is ChildSoloPlane || current.viewGroup !== parent) {
                    proxy.plane = ChildSoloPlane(proxy, parent)
                    return true
                }
            }
        }
    }

    return false
}

private fun rootPlane(proxy: ShadowProxy, target: View): Boolean {
    when {
        target.shadowPlane != ShadowPlane.Inline -> {
            handleError(proxy) {
                "Shadows on root Views must use the Inline plane"
            }
        }

        target.tintOutlineShadow && !target.clipOutlineShadow -> {
            handleError(proxy) {
                "Color compat shadows on root Views " +
                        "require that the clip feature be enabled too"
            }
        }

        target.clipToOutline -> {
            handleError(proxy) {
                "Shadows on root Views cannot have target.clipToOutline=true"
            }
        }

        target !is ViewGroup &&
                (Build.VERSION.SDK_INT == 28 || !RenderNodeFactory.isOpen) -> {
            handleError(proxy) {
                buildString {
                    append(
                        "Library shadows are not " +
                                "available on non-ViewGroup roots "
                    )
                    @SuppressLint("ObsoleteSdkInt")
                    if (Build.VERSION.SDK_INT == 28) {
                        append("on API level 28, Pie")
                    } else {
                        append("with the fallback draw method currently in use")
                    }
                }
            }
        }

        else -> {
            if (proxy.plane !is RootSoloPlane) {
                proxy.plane = RootSoloPlane(proxy)
                return true
            }
        }
    }

    return false
}

@OptIn(ExperimentalShadowGadgets::class)
private fun handleError(proxy: ShadowProxy, message: () -> String) {
    val target = proxy.target

    when {
        ShadowGadgets.throwOnUnhandledErrors &&
                target.onShadowModeChange == null ||
                target.isInEditMode -> {
            throw ShadowException("${target.debugId()}: ${message()}")
        }

        BuildConfig.DEBUG &&
                target.onShadowModeChange == null &&
                !ShadowGadgets.suppressLogs -> {
            Log.e("ShadowGadgets", "${target.debugId()}: ${message()}")
        }
    }

    proxy.plane = Plane.Null
}