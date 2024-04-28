package com.zedalpha.shadowgadgets.demo.topic

import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentPlaneBinding
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane

internal val PlaneTopic = Topic(
    "Plane",
    R.string.description_plane,
    PlaneFragment::class.java
)

class PlaneFragment : TopicFragment<FragmentPlaneBinding>(
    FragmentPlaneBinding::inflate
) {
    override fun loadUi(ui: FragmentPlaneBinding) {
        ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
            ui.motionView.clipOutlineShadow = isChecked
            ui.centerView.clipOutlineShadow = isChecked
        }
        ui.planeSelect.setOnCheckedChangeListener { _, checkedId ->
            val plane = when (checkedId) {
                R.id.foreground_selection -> ShadowPlane.Foreground
                R.id.background_selection -> ShadowPlane.Background
                else -> ShadowPlane.Inline
            }
            ui.motionView.shadowPlane = plane
            ui.centerView.shadowPlane = plane
        }
    }
}