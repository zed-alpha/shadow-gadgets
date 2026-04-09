package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentColorCompatDrawableBinding
import com.zedalpha.shadowgadgets.demo.internal.DemoShadowDrawable
import com.zedalpha.shadowgadgets.demo.internal.doOnUserChange
import android.graphics.Color as AndroidColor

internal val ColorCompatDrawableTopic =
    Topic(
        title = "ColorCompat: Drawable",
        descriptionResId = R.string.description_color_compat_drawable,
        fragmentClass = ColorCompatDrawableFragment::class.java
    )

class ColorCompatDrawableFragment :
    TopicFragment<FragmentColorCompatDrawableBinding>(
        inflate = FragmentColorCompatDrawableBinding::inflate
    ) {

    private lateinit var drawable: DemoShadowCompatDrawable

    override fun loadUi(ui: FragmentColorCompatDrawableBinding) {
        ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDrawable(isChecked)
        }
        ui.seekRotation.doOnUserChange { progress ->
            drawable.rotationZ = progress.toFloat()
            drawable.invalidateSelf()
        }
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
            DemoShadowCompatDrawable(ui.drawableView, clipped)
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

private class DemoShadowCompatDrawable(view: View, clipped: Boolean) :
    DemoShadowDrawable(view, clipped) {

    // This is probably the simplest way to position the shadow, as long as you
    // don't need to track the shadow's own local bounds. If that's required,
    // DemoClippedShadowDrawable shows how to use setPosition() instead.
    override fun centerShadow(bounds: Rect, sideLength: Float) {
        translationX = (bounds.width() - sideLength) / 2F
        translationY = (bounds.height() - sideLength) / 2F
    }
}