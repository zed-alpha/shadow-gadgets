package com.zedalpha.shadowgadgets.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.ShadowForge
import kotlin.math.roundToInt

open class ClippedShadowDrawable private constructor(
    private val clippedShadow: ClippedShadow
) : Drawable() {

    constructor(ownerView: View) :
            this(ShadowForge.createClippedShadow(ownerView))

    @RequiresApi(29)
    constructor() : this(ShadowForge.createClippedShadow())

    fun setPathProvider(provider: ((Path) -> Unit)?) {
        clippedShadow.pathProvider =
            if (provider != null) PathProvider(provider) else null
    }

    override fun setAlpha(alpha: Int) {
        clippedShadow.alpha = alpha / 255F
    }

    override fun getAlpha(): Int {
        return (255 * clippedShadow.alpha).roundToInt()
    }

    // We should be able to do most of this by interface delegation, but Kotlin
    // complains that we can't access Shadow without a dependency on core when
    // we try to subclass this later. I'm not sure that that's a valid error.

    var cameraDistance: Float by clippedShadow::cameraDistance

    var elevation: Float by clippedShadow::elevation

    var pivotX: Float by clippedShadow::pivotX

    var pivotY: Float by clippedShadow::pivotY

    var rotationX: Float by clippedShadow::rotationX

    var rotationY: Float by clippedShadow::rotationY

    var rotationZ: Float by clippedShadow::rotationZ

    var scaleX: Float by clippedShadow::scaleX

    var scaleY: Float by clippedShadow::scaleY

    var translationX: Float by clippedShadow::translationX

    var translationY: Float by clippedShadow::translationY

    var translationZ: Float by clippedShadow::translationZ

    var ambientColor: Int by clippedShadow::ambientColor

    var spotColor: Int by clippedShadow::spotColor

    fun hasIdentityMatrix(): Boolean =
        clippedShadow.hasIdentityMatrix()

    fun getMatrix(outMatrix: Matrix) {
        clippedShadow.getMatrix(outMatrix)
    }

    fun setOutline(outline: Outline?) {
        clippedShadow.setOutline(outline)
    }

    override fun draw(canvas: Canvas) {
        clippedShadow.draw(canvas)
    }

    fun dispose() {
        clippedShadow.dispose()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}