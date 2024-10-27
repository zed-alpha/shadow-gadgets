package com.zedalpha.shadowgadgets.demo.topic.compat

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.demo.databinding.ViewPanelBinding
import com.zedalpha.shadowgadgets.view.ShadowColorsBlender
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import android.graphics.Color as AndroidColor

internal class ViewIntroPanel(
    inflater: LayoutInflater,
    parent: ViewGroup
) : IntroPanel {

    private val blender = ShadowColorsBlender(parent.context)

    override val ui = ViewPanelBinding.inflate(inflater, parent, true)

    init {
        ui.compat.shadowPlane = ShadowPlane.Background
        ui.compat.forceOutlineShadowColorCompat = true
    }

    override var isShowingBackgrounds: Boolean
        get() = ui.normal.backgroundTintList == null
        set(isShown) {
            val csl = ColorStateList.valueOf(
                if (isShown) AndroidColor.WHITE else AndroidColor.TRANSPARENT
            )
            ui.normal.backgroundTintList = csl
            ui.compat.backgroundTintList = csl
        }

    override var elevation: Float
        get() = ui.normal.elevation
        set(value) {
            ui.normal.elevation = value
            ui.compat.elevation = value
        }

    override var outlineAmbientShadowColor: Int = AndroidColor.BLACK
        set(value) {
            if (field == value) return
            field = value
            if (Build.VERSION.SDK_INT >= 28) {
                ui.normal.outlineAmbientShadowColor = value
            }
            updateColorCompat()
        }

    override var outlineSpotShadowColor: Int = AndroidColor.BLACK
        set(value) {
            if (field == value) return
            field = value
            if (Build.VERSION.SDK_INT >= 28) {
                ui.normal.outlineSpotShadowColor = value
            }
            updateColorCompat()
        }

    // This demo only has one theme, so this will never actually
    // change anything. It's here simply to illustrate the usage.
    fun onConfigurationChanged() {
        blender.onConfigurationChanged()
        updateColorCompat()
    }

    private fun updateColorCompat() {
        ui.compat.outlineShadowColorCompat =
            blender.blend(outlineAmbientShadowColor, outlineSpotShadowColor)
    }
}