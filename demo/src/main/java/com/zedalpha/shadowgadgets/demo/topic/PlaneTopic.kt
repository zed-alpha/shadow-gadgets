package com.zedalpha.shadowgadgets.demo.topic

import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentPlaneBinding
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane

internal object PlaneTopic : Topic {

    override val title = "Plane"

    override val descriptionResId = R.string.description_plane

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_plane) {

        override fun loadUi(view: View) {
            val ui = FragmentPlaneBinding.bind(view)

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
}