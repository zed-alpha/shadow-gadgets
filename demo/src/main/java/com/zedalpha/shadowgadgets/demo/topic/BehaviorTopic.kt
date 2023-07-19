package com.zedalpha.shadowgadgets.demo.topic

import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentBehaviorBinding
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane

internal object BehaviorTopic : Topic {

    override val title = "Behavior"

    override val descriptionResId = R.string.description_behavior

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_behavior) {

        override fun loadUi(view: View) {
            val ui = FragmentBehaviorBinding.bind(view)

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.motionView.clipOutlineShadow = isChecked
                ui.blueView.clipOutlineShadow = isChecked
            }

            ui.planeSelect.setOnCheckedChangeListener { _, checkedId ->
                val plane = when (checkedId) {
                    R.id.foreground_radio -> ClippedShadowPlane.Foreground
                    else -> ClippedShadowPlane.Background
                }
                ui.motionView.clippedShadowPlane = plane
                ui.blueView.clippedShadowPlane = plane
            }
        }
    }
}