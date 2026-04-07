package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentViewDrawableBinding
import com.zedalpha.shadowgadgets.demo.internal.DemoShadowDrawable
import com.zedalpha.shadowgadgets.demo.internal.SeekChangeListener
import kotlin.math.roundToInt

internal val ViewDrawableTopic =
    Topic(
        title = "View: Drawable",
        descriptionResId = R.string.description_view_drawable,
        fragmentClass = ViewDrawableFragment::class.java
    )

class ViewDrawableFragment :
    TopicFragment<FragmentViewDrawableBinding>(
        inflate = FragmentViewDrawableBinding::inflate
    ) {

    private lateinit var syncedDrawable: DemoClippedShadowDrawable
    private lateinit var unsyncedDrawable: DemoClippedShadowDrawable

    override fun loadUi(ui: FragmentViewDrawableBinding) {
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

private class DemoClippedShadowDrawable(view: View) :
    DemoShadowDrawable(view, true) {

    init {
        ambientColor = Color.BLUE
        spotColor = Color.BLUE
    }

    // This is analogous to how a View positions its outline within its bounds.
    // setPosition() defines the shadow's position within the drawable's bounds.
    // Alternately, DemoShadowCompatDrawable's override uses only translations.
    override fun centerShadow(bounds: Rect, sideLength: Float) {
        val side = sideLength.roundToInt()
        val pos = Rect(0, 0, side, side)
        pos.offset((bounds.width() - side) / 2, (bounds.height() - side) / 2)
        setPosition(pos.left, pos.top, pos.right, pos.bottom)
    }
}