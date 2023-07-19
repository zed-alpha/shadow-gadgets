package com.zedalpha.shadowgadgets.demo.topic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentMotionBinding
import com.zedalpha.shadowgadgets.view.clipOutlineShadow

internal object MotionTopic : Topic {

    override val title = "Motion"

    override val descriptionResId = R.string.description_motion

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_motion) {

        override fun loadUi(view: View) {
            val ui = FragmentMotionBinding.bind(view)

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.motionView.clipOutlineShadow = isChecked
                ui.fabStart.clipOutlineShadow = isChecked
                ui.fabCenter.clipOutlineShadow = isChecked
                ui.fabEnd.clipOutlineShadow = isChecked
            }

            // CoordinatorLayout dance
            val snackbar = Snackbar.make(
                ui.fabStart,
                "I'm translucent!",
                Snackbar.LENGTH_SHORT
            )
            snackbar.view.apply {
                clipOutlineShadow = true
                backgroundTintList =
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.see_through_deep_blue
                    )
            }
            fun setShown(shown: Boolean) {
                if (shown) {
                    ui.fabStart.show(); ui.fabCenter.show(); ui.fabEnd.show()
                } else {
                    ui.fabStart.hide(); ui.fabCenter.hide(); ui.fabEnd.hide()
                }
            }

            val listener = object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    snackbar.dismiss()
                }
            }
            val callback = object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar?) {
                    setShown(false)
                    ui.fabCenter.postDelayed(1000) {
                        ui.fabStart.addOnShowAnimationListener(listener)
                        setShown(true)
                    }
                }
            }
            ui.fabStart.setOnClickListener {
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.removeCallback(callback)
                snackbar.show()
            }
            ui.fabCenter.setOnClickListener {
                snackbar.duration = Snackbar.LENGTH_INDEFINITE
                snackbar.addCallback(callback)
                snackbar.show()
            }
            ui.fabEnd.setOnClickListener {
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.removeCallback(callback)
                setShown(false)
                ui.root.postDelayed(1000) { setShown(true) }
            }
        }
    }
}