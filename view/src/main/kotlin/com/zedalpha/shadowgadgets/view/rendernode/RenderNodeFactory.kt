package com.zedalpha.shadowgadgets.view.rendernode

import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build

internal object RenderNodeFactory {

    val isOpen: Boolean =
        when {
            Build.VERSION.SDK_INT >= 29 -> true
            Build.VERSION.SDK_INT == 28 -> false
            else -> testInstance()
        }

    fun create(name: String? = null): RenderNodeWrapper {
        check(isOpen) { "Unavailable" }
        return createWrapper(name).apply { setClipToBounds(false) }
    }

    private fun createWrapper(name: String?): RenderNodeWrapper =
        when (Build.VERSION.SDK_INT) {
            21, 22 -> RenderNodeApi21(name)
            in 23..27 -> RenderNodeApi23(name)
            28 -> error("Unreachable")
            else -> RenderNodeApi29(name)
        }

    private fun testInstance(): Boolean =
        try {
            with(createWrapper("Tester")) {
                alpha = alpha
                cameraDistance = cameraDistance
                elevation = elevation
                pivotX = pivotX
                pivotY = pivotY
                rotationX = rotationX
                rotationY = rotationY
                rotationZ = rotationZ
                scaleX = scaleX
                scaleY = scaleY
                translationX = translationX
                translationY = translationY
                translationZ = translationZ
                ambientColor = ambientColor
                spotColor = spotColor
                // No tests for left, top, …; we use a Rect on these versions.
                setPosition(0, 0, 1, 1)
                setOutline(Outline())
                hasIdentityMatrix()
                val matrix = Matrix()
                getMatrix(matrix)
                getInverseMatrix(matrix)
                setClipToBounds(false)
                setProjectBackwards(true)
                setProjectionReceiver(true)
                // SIGSEGV on 21 if too early, before GLContext is valid.
                if (Build.VERSION.SDK_INT > 21) {
                    beginRecording()
                    endRecording()
                }
                hasDisplayList()
                discardDisplayList()
                setUseCompositingLayer(false, null)
            }
            true
        } catch (_: Throwable) {
            false
        }
}