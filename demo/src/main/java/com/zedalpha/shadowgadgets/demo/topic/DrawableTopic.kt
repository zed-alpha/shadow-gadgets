package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentDrawableBinding
import com.zedalpha.shadowgadgets.view.ClippedShadowDrawable


internal object DrawableTopic : Topic {

    override val title = "Drawable"

    override val descriptionResId = R.string.description_drawable

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_drawable) {

        private lateinit var syncedDrawable: DemoClippedShadowDrawable
        private lateinit var unsyncedDrawable: DemoClippedShadowDrawable

        override fun loadUi(view: View) {
            val ui = FragmentDrawableBinding.bind(view)

            syncedDrawable = DemoClippedShadowDrawable(ui.syncedView)
            ui.syncedView.background = syncedDrawable
            ui.syncedSeek.setOnSeekBarChangeListener(
                object : SeekChangeListener {
                    override fun onChange(progress: Int) {
                        syncedDrawable.rotationZ = progress.toFloat()
                        syncedDrawable.invalidateSelf()
                    }
                }
            )

            unsyncedDrawable = DemoClippedShadowDrawable(ui.unsyncedView)
            ui.unsyncedView.background = unsyncedDrawable
            ui.unsyncedSeek.setOnSeekBarChangeListener(
                object : SeekChangeListener {
                    override fun onChange(progress: Int) {
                        unsyncedDrawable.rotationZ = progress.toFloat()
                        // No call to invalidateSelf()
                    }
                }
            )
        }

        override fun onDestroyView() {
            super.onDestroyView()
            syncedDrawable.dispose()
            unsyncedDrawable.dispose()
        }
    }
}

private class DemoClippedShadowDrawable(view: View) :
    ClippedShadowDrawable(view) {

    private val path = Path()

    init {
        elevation = 40F
        setPathProvider { it.set(path) }
    }

    override fun onBoundsChange(bounds: Rect) {
        val sideLength = 0.5F * minOf(bounds.width(), bounds.height())
        pivotX = sideLength / 2F
        pivotY = sideLength / 2F
        translationX = (bounds.width() - sideLength) / 2F
        translationY = (bounds.height() - sideLength) / 2F

        val outline = Outline()
        if (Build.VERSION.SDK_INT >= 30) {
            path.setToPuzzlePiece(sideLength)
            outline.setPath(path)
        } else {
            path.setToCompassPointer(sideLength)
            @Suppress("DEPRECATION")
            outline.setConvexPath(path)
        }
        outline.alpha = 1.0F
        setOutline(outline)
    }
}

@RequiresApi(29)
private fun Path.setToPuzzlePiece(sideLength: Float) {
    val q = sideLength / 4

    // top
    reset()
    moveTo(0F, q)
    lineTo(q, q)
    arcTo(q, 0F, 2 * q, q, 100F, 340F, false)
    lineTo(2 * q, q)
    lineTo(3 * q, q)

    // right
    lineTo(3 * q, 2 * q)
    arcTo(3 * q, 2 * q, 4 * q, 3 * q, 190F, 340F, false)
    lineTo(3 * q, 3 * q)
    lineTo(3 * q, 4 * q)

    // bottom
    lineTo(2 * q, 4 * q)
    arcTo(q, 3 * q, 2 * q, 4 * q, 80F, -340F, false)
    lineTo(q, 4 * q)
    lineTo(0F, 4 * q)

    // left
    lineTo(0F, 3 * q)
    arcTo(0F, 2 * q, q, 3 * q, 170F, -340F, false)
    lineTo(0F, 2 * q)
    lineTo(0F, q)

    close()
}

private fun Path.setToCompassPointer(sideLength: Float) {
    val h = sideLength / 2

    reset()
    addRoundRect(
        0F,
        0F,
        sideLength,
        sideLength,
        floatArrayOf(0F, 0F, h, h, 0F, 0F, h, h),
        Path.Direction.CW
    )
}