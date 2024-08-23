package com.zedalpha.shadowgadgets.demo.topic.compat

import android.content.res.Configuration
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ViewSwitcher
import androidx.viewbinding.ViewBinding
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentCompatIntroBinding
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

internal val CompatIntroTopic = Topic(
    "Compat - Intro",
    R.string.description_compat_intro,
    CompatIntroFragment::class.java
)

class CompatIntroFragment : TopicFragment<FragmentCompatIntroBinding>(
    FragmentCompatIntroBinding::inflate
) {
    private lateinit var controller: PanelController

    override fun loadUi(ui: FragmentCompatIntroBinding) {
        controller = PanelController(
            ui.frame,
            true,
            AndroidColor.BLUE,
            AndroidColor.RED,
            50F
        )

        val ambientIndicator = PaintDrawable(AndroidColor.BLACK).apply {
            setBounds(0, 0, 50, 50)
            setCornerRadius(10F)
            ui.ambientSelection.setCompoundDrawables(null, null, this, null)
        }
        val spotIndicator = PaintDrawable(AndroidColor.BLACK).apply {
            setBounds(0, 0, 50, 50)
            setCornerRadius(10F)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        controller.onConfigurationChanged()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateFramework()
        ui.controls.syncColor()
        ui.controls.syncElevation()
    }

    private fun updateFramework() {
        controller.loadPanel(ui.frameworkSelect.checkedRadioButtonId)
    }
}

internal class PanelController(
    private val parent: ViewSwitcher,
    isShowingBackgrounds: Boolean,
    outlineAmbientShadowColor: Int,
    outlineSpotShadowColor: Int,
    elevation: Float
) {
    private val viewPanel =
        ViewIntroPanel(LayoutInflater.from(parent.context), parent)

    private val composePanel =
        ComposeIntroPanel(LayoutInflater.from(parent.context), parent)

    private var currentPanel: IntroPanel = viewPanel
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

    private fun sync(panel: IntroPanel) {
        panel.isShowingBackgrounds = isShowingBackgrounds
        panel.outlineAmbientShadowColor = outlineAmbientShadowColor
        panel.outlineSpotShadowColor = outlineSpotShadowColor
        panel.elevation = elevation
    }

    fun loadPanel(selection: Int) {
        val newPanel = when (selection) {
            R.id.view_selection -> viewPanel
            else -> composePanel
        }
        if (currentPanel != newPanel) currentPanel = newPanel
    }

    fun onConfigurationChanged() {
        viewPanel.onConfigurationChanged()
    }
}

internal interface IntroPanel {

    val ui: ViewBinding

    var isShowingBackgrounds: Boolean

    var outlineAmbientShadowColor: Int

    var outlineSpotShadowColor: Int

    var elevation: Float
}