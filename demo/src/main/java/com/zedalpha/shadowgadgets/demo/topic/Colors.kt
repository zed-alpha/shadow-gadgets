package com.zedalpha.shadowgadgets.demo.topic

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.ZedAlphaControl

@RequiresApi(Build.VERSION_CODES.P)
class ColorsFragment : TopicFragment(R.layout.fragment_colors) {
    override val targetIds =
        intArrayOf(
            R.id.view_colors,
            R.id.fab_colors_start,
            R.id.fab_colors_center,
            R.id.fab_colors_end
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fabStart = view.findViewById<View>(R.id.fab_colors_start)
        val fabCenter = view.findViewById<View>(R.id.fab_colors_center)
        val fabEnd = view.findViewById<View>(R.id.fab_colors_end)

        val exampleView = view.findViewById<View>(R.id.view_colors)
        exampleView.backgroundTintList = ColorStateList.valueOf(0x66ffffff)

        view.findViewById<ZedAlphaControl>(R.id.zac_colors).listener =
            object : ZedAlphaControl.Listener {
                override fun onElevationChange(elevation: Float) {
                    exampleView.elevation = elevation
                }

                override fun onColorChange(color: Int) {
                    exampleView.outlineShadowColor(color)
                    fabStart.outlineShadowColor(color)
                    fabCenter.outlineShadowColor(color)
                    fabEnd.outlineShadowColor(color)
                }
            }
    }

    private fun View.outlineShadowColor(@ColorInt color: Int) {
        outlineAmbientShadowColor = color
        outlineSpotShadowColor = color
    }
}