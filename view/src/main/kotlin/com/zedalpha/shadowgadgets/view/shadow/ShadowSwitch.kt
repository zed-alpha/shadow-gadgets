package com.zedalpha.shadowgadgets.view.shadow

import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.view.BuildConfig
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane.Background
import com.zedalpha.shadowgadgets.view.ShadowPlane.Inline
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.inlineController
import java.lang.ref.WeakReference

internal fun View.checkShadow() {
    if (clipOutlineShadow || colorOutlineShadow) {
        if (isWatched) {
            shadow?.run { if (isClipped != clipOutlineShadow) recreateShadow() }
        } else {
            isWatched = true
            addOnAttachStateChangeListener(ShadowSwitch)
            if (isAttachedToWindow) ShadowSwitch.onViewAttachedToWindow(this)
        }
    } else if (isWatched) {
        isWatched = false
        removeOnAttachStateChangeListener(ShadowSwitch)
        shadow?.detachFromTarget()
    }
}

private object ShadowSwitch : View.OnAttachStateChangeListener {

    override fun onViewAttachedToWindow(view: View) {
        val shadow = view.shadow
        val scope = view.parent as? ViewGroup

        if (shadow == null || view.shadowScope !== scope) {
            shadow?.detachFromTarget()
            view.shadowScope = scope

            val isRecyclingViewGroup = scope?.isRecyclingViewGroup == true
            view.isRecyclingViewGroupChild = isRecyclingViewGroup

            if (!(scope is ShadowsViewGroup && isRecyclingViewGroup) ||
                view.isInitialized
            ) {
                view.createShadow()
            }
        } else {
            shadow.isShown = true
        }
    }

    override fun onViewDetachedFromWindow(view: View) {
        val shadow = view.shadow ?: return

        if (view.isRecyclingViewGroupChild) {
            shadow.isShown = false
        } else {
            shadow.detachFromTarget()
        }
    }
}

internal fun View.recreateShadow() {
    shadow?.detachFromTarget() ?: return
    createShadow()
}

internal fun View.createShadow() =
    shadowScope?.let { childShadow(it) } ?: rootShadow()

private fun View.childShadow(scope: ViewGroup) {
    when {
        colorOutlineShadow && !clipOutlineShadow && shadowPlane != Background -> {
            nullShadow { "Color compat by itself must use the Background plane" }
        }
        shadowPlane != Inline -> {
            scope.getOrCreateOverlayController().createShadow(this)
        }
        scope is ShadowsViewGroup && !scope.ignoreInlineChildShadows -> {
            scope.inlineController.createShadow(this)
        }
        clipToOutline || scope.clipChildren -> {
            fun message() = buildString {
                append("Inline shadow ")
                if (clipToOutline) append("target has clipToOutline=true")
                if (clipToOutline && scope.clipChildren) append(" and ")
                if (scope.clipChildren) append("parent has clipChildren=true")
            }
            nullShadow(::message)
        }
        else -> SoloController(this, scope).shadow
    }
}

private fun View.rootShadow() {
    when {
        colorOutlineShadow && !clipOutlineShadow -> {
            nullShadow { "Color compat on root Views requires the clip too" }
        }
        shadowPlane != Inline -> {
            nullShadow { "Shadows on root Views must use the Inline plane" }
        }
        clipToOutline -> {
            nullShadow { "Inline shadow target has clipToOutline=true" }
        }
        else -> SoloController(this, null).shadow
    }
}

private fun View.nullShadow(logMessage: () -> String) {
    if (BuildConfig.DEBUG) Log.w("ShadowGadgets", "$debugName: ${logMessage()}")
    NullShadow(this)
}

internal var View.isInitialized: Boolean
    get() = getTag(R.id.is_initialized) == true
    set(value) = setTag(R.id.is_initialized, value)

internal val ViewGroup.isRecyclingViewGroup: Boolean
    get() = this is RecyclerView || this is AdapterView<*>

private var View.isWatched: Boolean
    get() = getTag(R.id.is_watched) == true
    set(value) = setTag(R.id.is_watched, value)

@get:Suppress("UNCHECKED_CAST")
private var View.shadowScope: ViewGroup?
    get() = (getTag(R.id.shadow_scope) as? WeakReference<ViewGroup>)?.get()
    set(value) = setTag(R.id.shadow_scope, value?.let { WeakReference(it) })

private var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.is_recycling_view_group_child) == true
    set(value) = setTag(R.id.is_recycling_view_group_child, value)

private val View.debugName: String
    get() = buildString {
        append(this@debugName.javaClass.simpleName)
        if (id != View.NO_ID) try {
            append(", R.id.${resources.getResourceEntryName(id)}")
        } catch (_: Resources.NotFoundException) {
            ", id=$id"
        }
    }