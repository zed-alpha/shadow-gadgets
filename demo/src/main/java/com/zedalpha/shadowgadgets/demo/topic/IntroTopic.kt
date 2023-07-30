package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentIntroBinding
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat


internal object IntroTopic : Topic {

    override val title = "Intro"

    override val descriptionResId = R.string.description_intro

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_intro) {

        override fun loadUi(view: View) {
            val ui = FragmentIntroBinding.bind(view)

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.target.clipOutlineShadow = isChecked
            }

            // Drag and drop
            val dragListener = SimpleDragListener(ui.target)
            ui.frameOne.setOnDragListener(dragListener)
            ui.frameTwo.setOnDragListener(dragListener)
            ui.target.setOnLongClickListener { target ->
                target.startDragging(GrayShadow(target), target.parent)
                true
            }

            // Color and elevation
            val seekListener = object : SeekChangeListener {
                override fun onChange(progress: Int) = updateTarget(ui)
            }
            ui.seekAlpha.setOnSeekBarChangeListener(seekListener)
            ui.seekRed.setOnSeekBarChangeListener(seekListener)
            ui.seekGreen.setOnSeekBarChangeListener(seekListener)
            ui.seekBlue.setOnSeekBarChangeListener(seekListener)
            ui.seekElevation.setOnSeekBarChangeListener(seekListener)
            ui.colorSelect.setOnCheckedChangeListener { _, checkedId ->
                val color = if (checkedId == R.id.view_radio) {
                    ui.target.tag as? Int ?: Color.TRANSPARENT
                } else if (Build.VERSION.SDK_INT >= 28) {
                    ui.target.outlineAmbientShadowColor
                } else {
                    ui.target.outlineShadowColorCompat
                }
                ui.seekAlpha.progress = Color.alpha(color)
                ui.seekRed.progress = Color.red(color)
                ui.seekGreen.progress = Color.green(color)
                ui.seekBlue.progress = Color.blue(color)
            }
            updateTarget(ui)
        }

        private fun updateTarget(ui: FragmentIntroBinding) {
            val color = Color.argb(
                ui.seekAlpha.progress,
                ui.seekRed.progress,
                ui.seekGreen.progress,
                ui.seekBlue.progress
            )
            if (ui.colorSelect.checkedRadioButtonId == R.id.view_radio) {
                ui.target.apply { background.setTint(color); tag = color }
            } else if (Build.VERSION.SDK_INT >= 28) {
                ui.target.outlineAmbientShadowColor = color
                ui.target.outlineSpotShadowColor = color
            } else {
                ui.target.outlineShadowColorCompat = color
            }
            ui.target.elevation = ui.seekElevation.progress.toFloat()
        }
    }
}

private fun View.startDragging(
    shadowBuilder: View.DragShadowBuilder?,
    myLocalState: Any?,
) {
    if (Build.VERSION.SDK_INT >= 24) {
        startDragAndDrop(null, shadowBuilder, myLocalState, 0)
    } else {
        @Suppress("DEPRECATION")
        startDrag(null, shadowBuilder, myLocalState, 0)
    }
}

private class GrayShadow(private val target: View) :
    View.DragShadowBuilder(target) {

    val paint = Paint().apply { color = Color.LTGRAY }

    val radius = target.resources.getDimension(R.dimen.target_corner_radius)

    override fun onDrawShadow(canvas: Canvas) {
        canvas.drawRoundRect(
            0F,
            0F,
            target.width.toFloat(),
            target.height.toFloat(),
            radius,
            radius,
            paint
        )
    }
}

private class SimpleDragListener(private val target: View) :
    View.OnDragListener {

    override fun onDrag(viewOver: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (viewOver == event.localState) viewOver.scale(0.95F)
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                if (viewOver != event.localState) viewOver.scale(1.05F)
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                if (viewOver != event.localState) viewOver.scale(1.0F)
            }

            DragEvent.ACTION_DROP -> {
                if (viewOver != event.localState) {
                    (event.localState as ViewGroup).removeView(target)
                    (viewOver as ViewGroup).addView(target)
                    viewOver.scale(1.0F)
                }
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                if (viewOver == event.localState) {
                    val oldParent = event.localState as ViewGroup
                    oldParent.scale(1.0F)
                }
            }
        }
        return true
    }
}

private fun View.scale(scale: Float) {
    scaleX = scale; scaleY = scale
}