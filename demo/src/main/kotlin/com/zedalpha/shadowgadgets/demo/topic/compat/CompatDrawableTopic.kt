package com.zedalpha.shadowgadgets.demo.topic.compat

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentCompatDrawableBinding
import com.zedalpha.shadowgadgets.demo.topic.SeekChangeListener
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import com.zedalpha.shadowgadgets.demo.topic.setToCompassPointer
import com.zedalpha.shadowgadgets.demo.topic.setToPuzzlePiece
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable
import android.graphics.Color as AndroidColor

internal val CompatDrawableTopic = Topic(
    "Compat - Drawable",
    R.string.description_compat_drawable,
    CompatDrawableFragment::class.java
)

class CompatDrawableFragment : TopicFragment<FragmentCompatDrawableBinding>(
    FragmentCompatDrawableBinding::inflate
) {
    private lateinit var drawable: DemoCompatShadowDrawable

    override fun loadUi(ui: FragmentCompatDrawableBinding) {
        ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDrawable(isChecked)
        }
        ui.seekRotation.setOnSeekBarChangeListener(
            SeekChangeListener { progress ->
                drawable.rotationZ = progress.toFloat()
                drawable.invalidateSelf()
            }
        )
        ui.controls.apply {
            onColorChanged { color ->
                drawable.colorCompat = color
                drawable.invalidateSelf()
            }
            onElevationChanged { elevation ->
                drawable.elevation = elevation.toFloat()
                drawable.invalidateSelf()
            }
            color = AndroidColor.BLUE
            elevation = 30
        }
    }

    private fun setDrawable(clipped: Boolean) {
        if (::drawable.isInitialized) drawable.dispose()
        drawable =
            DemoCompatShadowDrawable(ui.drawableView, clipped)
                .also { ui.drawableView.background = it }
        ui.controls.syncColor()
        ui.controls.syncElevation()
        drawable.rotationZ = ui.seekRotation.progress.toFloat()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setDrawable(ui.clipSwitch.isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawable.dispose()
    }
}

private class DemoCompatShadowDrawable(view: View, clipped: Boolean) :
    ShadowDrawable(view, clipped) {

    private val path = Path()

    init {
        elevation = 40F
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