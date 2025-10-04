package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentIntroBinding
import com.zedalpha.shadowgadgets.demo.internal.DefaultTargetColor
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat

internal val IntroTopic =
    Topic(
        title = "Intro",
        descriptionResId = R.string.description_intro,
        fragmentClass = IntroFragment::class.java
    )

class IntroFragment :
    TopicFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {

    private var shadowColor: Int = Color.BLACK
        set(color) {
            field = color
            if (Build.VERSION.SDK_INT >= 28) {
                ui.target.outlineAmbientShadowColor = color
                ui.target.outlineSpotShadowColor = color
            } else {
                ui.target.outlineShadowColorCompat = color
            }
        }

    private var viewColor: Int = DefaultTargetColor
        set(color) {
            field = color
            ui.target.background.setTint(color)
        }

    override fun loadUi(ui: FragmentIntroBinding) {
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
        ui.colorSelect.setOnCheckedChangeListener { _, checkedId ->
            ui.controls.color =
                if (checkedId == R.id.view_selection) viewColor else shadowColor
        }
        ui.controls.apply {
            onColorChanged { color ->
                if (ui.colorSelect.checkedRadioButtonId == R.id.shadow_selection) {
                    shadowColor = color
                } else {
                    viewColor = color
                }
            }
            onElevationChanged { ui.target.elevation = it.toFloat() }
            color = shadowColor
            elevation = 50
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("reparent", ui.target.parent == ui.frameTwo)
        outState.putInt("shadow_color", shadowColor)
        outState.putInt("view_color", viewColor)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState?.getBoolean("reparent") == true) {
            ui.frameOne.removeView(ui.target)
            ui.frameTwo.addView(ui.target)
        }
        viewColor = savedInstanceState?.getInt("view_color") ?: viewColor
        savedInstanceState?.getInt("shadow_color")?.let { color ->
            shadowColor = color
        }
        ui.controls.syncElevation()
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
        val right = target.width.toFloat()
        val bottom = target.height.toFloat()
        canvas.drawRoundRect(0F, 0F, right, bottom, radius, radius, paint)
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