package com.zedalpha.shadowgadgets.demo.topic

import android.annotation.SuppressLint
import android.graphics.drawable.PaintDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ViewSwitcher
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentColorIntroBinding
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

internal val ColorIntroTopic =
    Topic(
        title = "Color: Intro",
        descriptionResId = R.string.description_color_intro,
        fragmentClass = ColorIntroFragment::class.java
    )

class ColorIntroFragment :
    TopicFragment<FragmentColorIntroBinding>(
        inflate = FragmentColorIntroBinding::inflate
    ) {

    private lateinit var controller: PanelController

    override fun loadUi(ui: FragmentColorIntroBinding) {
        @SuppressLint("SetTextI18n")
        if (Build.VERSION.SDK_INT < 28) ui.labelNative.text = "SDK < 28"

        controller =
            PanelController(
                parent = ui.frame,
                isShowingBackgrounds = true,
                outlineAmbientShadowColor = AndroidColor.BLUE,
                outlineSpotShadowColor = AndroidColor.RED,
                elevation = 50F
            )

        val ambientIndicator =
            PaintDrawable(AndroidColor.BLACK).apply {
                setCornerRadius(10F)
                setBounds(0, 0, 50, 50)
                ui.ambientSelection.setCompoundDrawables(null, null, this, null)
            }
        val spotIndicator =
            PaintDrawable(AndroidColor.BLACK).apply {
                setCornerRadius(10F)
                setBounds(0, 0, 50, 50)
                ui.spotSelection.setCompoundDrawables(null, null, this, null)
            }

        ui.frameworkSelect.setOnCheckedChangeListener { _, _ ->
            updateFramework()
        }
        ui.backgroundsSwitch.setOnCheckedChangeListener { _, isChecked ->
            controller.isShowingBackgrounds = isChecked
        }
        ui.colorSelect.setOnCheckedChangeListener { _, checkedId ->
            ui.controls.color = if (checkedId == R.id.ambient_selection) {
                controller.outlineAmbientShadowColor
            } else {
                controller.outlineSpotShadowColor
            }
        }

        ui.controls.onColorChanged { color ->
            if (ui.colorSelect.checkedRadioButtonId == R.id.ambient_selection) {
                controller.outlineAmbientShadowColor = color
                ambientIndicator.setTint(color)
            } else {
                controller.outlineSpotShadowColor = color
                spotIndicator.setTint(color)
            }
        }
        ui.controls.onElevationChanged { elevation ->
            controller.elevation = elevation.toFloat()
        }

        ui.controls.elevation = controller.elevation.roundToInt()
        ui.controls.color = controller.outlineAmbientShadowColor
        spotIndicator.setTint(controller.outlineSpotShadowColor)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateFramework()
        ui.controls.syncColor()
        ui.controls.syncElevation()
    }

    private fun updateFramework() =
        controller.loadPanel(ui.frameworkSelect.checkedRadioButtonId)
}

internal class PanelController(
    private val parent: ViewSwitcher,
    isShowingBackgrounds: Boolean,
    outlineAmbientShadowColor: Int,
    outlineSpotShadowColor: Int,
    elevation: Float
) {
    private val viewPanel =
        ViewColorIntroPanel(LayoutInflater.from(parent.context), parent)

    private val composePanel =
        ComposeColorIntroPanel(LayoutInflater.from(parent.context), parent)

    private var currentPanel: ColorIntroPanel = viewPanel
        set(newPanel) {
            field = newPanel
            sync(newPanel)
            parent.displayedChild = parent.indexOfChild(newPanel.ui.root)
        }

    var isShowingBackgrounds = isShowingBackgrounds
        set(value) {
            field = value
            currentPanel.isShowingBackgrounds = value
        }

    var outlineAmbientShadowColor = outlineAmbientShadowColor
        set(value) {
            field = value
            currentPanel.outlineAmbientShadowColor = value
        }

    var outlineSpotShadowColor = outlineSpotShadowColor
        set(value) {
            field = value
            currentPanel.outlineSpotShadowColor = value
        }

    var elevation = elevation
        set(value) {
            field = value
            currentPanel.elevation = value
        }

    init {
        sync(viewPanel)
    }

    private fun sync(panel: ColorIntroPanel) {
        panel.isShowingBackgrounds = isShowingBackgrounds
        panel.outlineAmbientShadowColor = outlineAmbientShadowColor
        panel.outlineSpotShadowColor = outlineSpotShadowColor
        panel.elevation = elevation
    }

    fun loadPanel(selection: Int) {
        currentPanel =
            if (selection == R.id.view_selection) viewPanel else composePanel
    }
}