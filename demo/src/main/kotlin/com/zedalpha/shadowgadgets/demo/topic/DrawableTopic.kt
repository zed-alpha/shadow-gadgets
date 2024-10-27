package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Color
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentDrawableBinding
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable

internal val DrawableTopic = Topic(
    "Drawable",
    R.string.description_drawable,
    DrawableFragment::class.java
)

class DrawableFragment : TopicFragment<FragmentDrawableBinding>(
    FragmentDrawableBinding::inflate
) {
    private lateinit var syncedDrawable: DemoClippedShadowDrawable
    private lateinit var unsyncedDrawable: DemoClippedShadowDrawable

    override fun loadUi(ui: FragmentDrawableBinding) {
        syncedDrawable = DemoClippedShadowDrawable(ui.viewSynced)
        ui.viewSynced.background = syncedDrawable
        ui.seekSynced.setOnSeekBarChangeListener(
            SeekChangeListener { progress ->
                syncedDrawable.rotationZ = progress.toFloat()
                syncedDrawable.invalidateSelf()
            }
        )

        unsyncedDrawable = DemoClippedShadowDrawable(ui.viewUnsynced)
        ui.viewUnsynced.background = unsyncedDrawable
        ui.seekUnsynced.setOnSeekBarChangeListener(
            SeekChangeListener { progress ->
                unsyncedDrawable.rotationZ = progress.toFloat()
                // No call to invalidateSelf()
            }
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        syncedDrawable.rotationZ = ui.seekSynced.progress.toFloat()
        unsyncedDrawable.rotationZ = ui.seekUnsynced.progress.toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncedDrawable.dispose()
        unsyncedDrawable.dispose()
    }
}

private class DemoClippedShadowDrawable(
    view: View
) : ShadowDrawable(view, true) {

    private val path = Path()

    init {
        elevation = 40F
        ambientColor = Color.BLUE
        spotColor = Color.BLUE
        setClipPathProvider { it.set(path) }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

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
internal fun Path.setToPuzzlePiece(sideLength: Float) {
    val q = sideLength / 4

    reset()

    // top
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

internal fun Path.setToCompassPointer(sideLength: Float) {
    val h = sideLength / 2

    reset()
    addRoundRect(
        0F, 0F,
        sideLength, sideLength,
        floatArrayOf(0F, 0F, h, h, 0F, 0F, h, h),
        Path.Direction.CW
    )
}