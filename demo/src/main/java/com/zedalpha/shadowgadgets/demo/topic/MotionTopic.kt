package com.zedalpha.shadowgadgets.demo.topic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentMotionBinding
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


internal object MotionTopic : Topic {

    override val title = "Motion"

    override val descriptionResId = R.string.description_motion

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_motion) {

        @SuppressLint("ShowToast")
        override fun loadUi(view: View) {
            val ui = FragmentMotionBinding.bind(view)

            val snackbar = Snackbar.make(
                ui.fabStart,
                "I'm translucent!",
                Snackbar.LENGTH_SHORT
            )
            snackbar.setTextColor(Color.BLACK)
            snackbar.view.backgroundTintList =
                ColorStateList.valueOf(DefaultTargetColor)

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.motionView.clipOutlineShadow = isChecked
                ui.fabStart.clipOutlineShadow = isChecked
                ui.fabCenter.clipOutlineShadow = isChecked
                ui.fabEnd.clipOutlineShadow = isChecked
                snackbar.view.clipOutlineShadow = isChecked
            }

            // Race conditions still possible, but it's good enough for a
            // simple demonstration. Just don't whack-a-mole the buttons.
            var animating = false

            ui.fabStart.setOnClickListener {
                if (animating) return@setOnClickListener
                viewLifecycleOwner.lifecycleScope.launch {
                    animating = true
                    snackbar.showAndAwait()
                    delay(1000)
                    snackbar.dismiss()
                    animating = false
                }
            }
            ui.fabCenter.setOnClickListener {
                if (animating) return@setOnClickListener
                viewLifecycleOwner.lifecycleScope.launch {
                    animating = true
                    snackbar.showAndAwait()
                    delay(20)
                    ui.fabStart.hide(); ui.fabCenter.hide(); ui.fabEnd.hide()
                    delay(1000)
                    ui.fabStart.show(); ui.fabCenter.show()
                    ui.fabEnd.showAndAwait()
                    delay(20)
                    snackbar.dismiss()
                    animating = false
                }
            }
            ui.fabEnd.setOnClickListener {
                if (animating) return@setOnClickListener
                viewLifecycleOwner.lifecycleScope.launch {
                    animating = true
                    ui.fabStart.hide(); ui.fabCenter.hide(); ui.fabEnd.hide()
                    delay(1000)
                    ui.fabStart.show(); ui.fabCenter.show(); ui.fabEnd.show()
                    animating = false
                }
            }
        }
    }
}

private suspend fun Snackbar.showAndAwait() =
    suspendCancellableCoroutine { continuation ->
        val callback = object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                removeCallback(this)
                continuation.resume(Unit)
            }
        }
        duration = Snackbar.LENGTH_INDEFINITE
        addCallback(callback)
        show()
    }

private suspend fun FloatingActionButton.showAndAwait() =
    suspendCancellableCoroutine { continuation ->
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (continuation.isActive) continuation.cancel()
            }

            override fun onAnimationEnd(animation: Animator) {
                removeOnShowAnimationListener(this)
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
        continuation.invokeOnCancellation {
            removeOnShowAnimationListener(listener)
        }
        addOnShowAnimationListener(listener)
        show()
    }