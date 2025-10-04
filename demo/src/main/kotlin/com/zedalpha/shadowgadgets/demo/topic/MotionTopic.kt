package com.zedalpha.shadowgadgets.demo.topic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentMotionBinding
import com.zedalpha.shadowgadgets.demo.internal.DefaultTargetColor
import com.zedalpha.shadowgadgets.demo.internal.toColorStateList
import com.zedalpha.shadowgadgets.demo.topic.Action.Hide
import com.zedalpha.shadowgadgets.demo.topic.Action.Show
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal val MotionTopic =
    Topic(
        title = "Motion",
        descriptionResId = R.string.description_motion,
        fragmentClass = MotionFragment::class.java
    )

class MotionFragment :
    TopicFragment<FragmentMotionBinding>(FragmentMotionBinding::inflate) {

    override fun loadUi(ui: FragmentMotionBinding) {
        val snackbar =
            Snackbar.make(ui.fabUp, "I'm translucent!", LENGTH_INDEFINITE)
        snackbar.view.backgroundTintList = DefaultTargetColor.toColorStateList()
        snackbar.setTextColor(Color.BLACK)

        ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
            ui.motionView.clipOutlineShadow = isChecked
            ui.fabUp.clipOutlineShadow = isChecked
            ui.fabBoth.clipOutlineShadow = isChecked
            ui.fabHide.clipOutlineShadow = isChecked
            snackbar.view.clipOutlineShadow = isChecked
        }

        ui.fabUp.setAnimations {
            snackbar.await(Show)
            delay(500)
            snackbar.await(Hide)
        }
        ui.fabBoth.setAnimations {
            snackbar.await(Show)
            ui.fabUp.hide(); ui.fabBoth.hide(); ui.fabHide.await(Hide)
            delay(500)
            ui.fabUp.show(); ui.fabBoth.show(); ui.fabHide.await(Show)
            snackbar.await(Hide)
        }
        ui.fabHide.setAnimations {
            ui.fabUp.hide(); ui.fabBoth.hide(); ui.fabHide.await(Hide)
            delay(500)
            ui.fabUp.show(); ui.fabBoth.show(); ui.fabHide.await(Show)
        }
    }

    private var animating = false

    private fun View.setAnimations(block: suspend () -> Unit) {
        setOnClickListener {
            if (animating) return@setOnClickListener

            animating = true
            viewLifecycleOwner.lifecycleScope.launch {
                block()
                animating = false
            }
        }
    }
}

private enum class Action { Show, Hide }

private suspend fun Snackbar.await(action: Action) =
    suspendCancellableCoroutine { continuation ->
        val callback =
            object : Snackbar.Callback() {

                override fun onShown(sb: Snackbar?) {
                    removeCallback(this)
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onDismissed(sb: Snackbar?, event: Int) =
                    onShown(sb)
            }
        continuation.invokeOnCancellation { removeCallback(callback) }
        addCallback(callback)
        if (action == Show) show() else dismiss()
    }

private suspend fun FloatingActionButton.await(action: Action) =
    suspendCancellableCoroutine { continuation ->
        val listener =
            object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    removeOnShowAnimationListener(this)
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onAnimationCancel(animation: Animator) =
                    onAnimationEnd(animation)
            }
        continuation.invokeOnCancellation {
            removeOnShowAnimationListener(listener)
        }
        if (action == Show) {
            addOnShowAnimationListener(listener)
            show()
        } else {
            addOnHideAnimationListener(listener)
            hide()
        }
    }