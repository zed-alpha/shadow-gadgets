package com.zedalpha.shadowgadgets.demo.topic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.demo.R

class MotionsFragment : TopicFragment(R.layout.fragment_motions) {
    override val targetIds = intArrayOf(
        R.id.view_motion,
        R.id.fab_motions_start,
        R.id.fab_motions_center,
        R.id.fab_motions_end
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabStart = view.findViewById<FloatingActionButton>(R.id.fab_motions_start)
        val fabCenter = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_motions_center)
        val fabEnd = view.findViewById<FloatingActionButton>(R.id.fab_motions_end)

        val snackbar = Snackbar.make(fabStart, "I'm translucent!", Snackbar.LENGTH_SHORT)
        snackbar.view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.see_through_deep_blue)
        )
        snackbar.view.clipOutlineShadow = true

        val callback = object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                fabStart.hide(); fabCenter.hide(); fabEnd.hide()
                fabCenter.postDelayed(1000) {
                    fabStart.addOnShowAnimationListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            snackbar.dismiss()
                        }
                    })
                    fabStart.show(); fabCenter.show(); fabEnd.show()
                }
            }
        }

        fabStart.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_SHORT
            snackbar.removeCallback(callback)
            snackbar.show()
        }
        fabCenter.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_INDEFINITE
            snackbar.addCallback(callback)
            snackbar.show()
        }
        fabEnd.setOnClickListener {
            snackbar.duration = Snackbar.LENGTH_SHORT
            snackbar.removeCallback(callback)
            fabStart.hide(); fabCenter.hide(); fabEnd.hide()
            fabCenter.postDelayed(1000) {
                fabStart.show(); fabCenter.show(); fabEnd.show()
            }
        }
    }
}