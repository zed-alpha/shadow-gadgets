package com.zedalpha.shadowgadgets.demo.topic

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.ZedAlphaControl


class IntroFragment : TopicFragment(R.layout.fragment_intro) {
    override val targetIds = intArrayOf(R.id.view_intro, R.id.view_drag_drop)

    private var dragDropParentId = R.id.frame_drag_drop_one

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exampleView = view.findViewById<View>(R.id.view_intro)
        val dragDropView = view.findViewById<View>(R.id.view_drag_drop)
        view.findViewById<ZedAlphaControl>(R.id.zac_intro).listener =
            object : ZedAlphaControl.Listener {
                override fun onElevationChange(elevation: Float) {
                    exampleView.elevation = elevation
                    dragDropView.elevation = elevation
                }

                override fun onColorChange(color: Int) {
                    val tint = ColorStateList.valueOf(color)
                    exampleView.backgroundTintList = tint
                    dragDropView.backgroundTintList = tint
                }
            }

        val frameOne = view.findViewById<FrameLayout>(R.id.frame_drag_drop_one)
        val frameTwo = view.findViewById<FrameLayout>(R.id.frame_drag_drop_two)
        val frameThree = view.findViewById<FrameLayout>(R.id.frame_drag_drop_three)
        val startingId = savedInstanceState?.getInt(STATE_DRAG_DROP_PARENT) ?: dragDropParentId
        if (startingId != 0 && startingId != R.id.frame_drag_drop_one) {
            view.findViewById<ViewGroup>(startingId)?.let { group ->
                frameOne.removeView(dragDropView)
                group.addView(dragDropView)
            }
        }
        dragDropView.setOnLongClickListener {
            val dragShadowBuilder = View.DragShadowBuilder(dragDropView)
            val parent = dragDropView.parent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dragDropView.startDragAndDrop(null, dragShadowBuilder, parent, 0)
            } else {
                @Suppress("DEPRECATION")
                dragDropView.startDrag(null, dragShadowBuilder, parent, 0)
            }
            dragDropView.isVisible = false
            true
        }
        val listener = View.OnDragListener { viewOver, event ->
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
                        (event.localState as ViewGroup).removeView(dragDropView)
                        (viewOver as ViewGroup).addView(dragDropView)
                        viewOver.scale(1.0F)
                        dragDropParentId = viewOver.id
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    if (viewOver == event.localState) {
                        dragDropView.isVisible = true
                        val oldParent = event.localState as ViewGroup
                        oldParent.scale(1.0F)
                    }
                }
            }
            true
        }
        frameOne.setOnDragListener(listener)
        frameTwo.setOnDragListener(listener)
        frameThree.setOnDragListener(listener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val parent = view?.findViewById<View>(R.id.view_drag_drop)?.parent as? View
        outState.putInt(STATE_DRAG_DROP_PARENT, parent?.id ?: 0)
    }

    private fun View.scale(scale: Float) {
        scaleX = scale
        scaleY = scale
    }
}

private const val STATE_DRAG_DROP_PARENT = "drag_drop_parent"