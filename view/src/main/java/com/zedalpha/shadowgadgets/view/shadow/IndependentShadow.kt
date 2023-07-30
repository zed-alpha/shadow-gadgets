package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.BuildConfig
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable


internal class IndependentShadow(
    targetView: View
) : ViewShadow(targetView) {

    private val isShown = canDrawAround(targetView)

    private val shadowDrawable = object : BaseDrawable() {
        override fun draw(canvas: Canvas) {
            update()
            shadow.draw(canvas)
        }
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (isShown && checkInvalidate()) shadowDrawable.invalidateSelf()
        true
    }

    init {
        if (isShown) with(targetView) {
            viewTreeObserver.addOnPreDrawListener(preDrawListener)
            overlay.add(shadowDrawable)
            invalidate()
        }
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        if (isShown) with(targetView) {
            viewTreeObserver.removeOnPreDrawListener(preDrawListener)
            overlay.remove(shadowDrawable)
            invalidate()
        }
    }

    private fun checkInvalidate(): Boolean {
        val target = targetView
        val shadow = shadow

        if (shadow.alpha != target.alpha) return true
        if (shadow.cameraDistance != target.cameraDistance) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.translationZ != target.translationZ) return true
        if (Build.VERSION.SDK_INT >= 28) {
            if (shadow.ambientColor !=
                ViewShadowColorsHelper.getAmbientColor(target)
            ) return true
            if (shadow.spotColor !=
                ViewShadowColorsHelper.getSpotColor(target)
            ) return true
        }
        return false
    }

    private fun update() {
        val target = targetView
        val shadow = shadow

        shadow.alpha = target.alpha
        shadow.cameraDistance = target.cameraDistance
        shadow.elevation = target.elevation
        shadow.translationZ = target.translationZ
        if (Build.VERSION.SDK_INT >= 28) {
            shadow.ambientColor =
                ViewShadowColorsHelper.getAmbientColor(target)
            shadow.spotColor =
                ViewShadowColorsHelper.getSpotColor(target)
        }
    }
}

private fun canDrawAround(targetView: View): Boolean {
    val badVersion = Build.VERSION.SDK_INT in 24..28
    val clipChildren = (targetView.parent as? ViewGroup)?.clipChildren == true
    val clipToOutline = targetView.clipToOutline
    return if (badVersion || clipChildren || clipToOutline) {
        if (BuildConfig.DEBUG) {
            val message = buildString {
                append("Inline shadow on ${targetView.debugName}: Added ")
                if (badVersion) append("in non-library or ignoreInlineChildShadows parent, API 24..28")
                if (clipChildren) append("in parent with clipChildren=true")
                if (clipChildren && clipToOutline) append(", and ")
                if (clipToOutline) append("on target with clipToOutline=true")
            }
            Log.w("ShadowGadgets", message)
        }
        false
    } else true
}

private val View.debugName: String
    get() = buildString {
        append(this@debugName.javaClass.simpleName)
        if (id != View.NO_ID) append(" R.id.${resources.getResourceEntryName(id)}")
    }