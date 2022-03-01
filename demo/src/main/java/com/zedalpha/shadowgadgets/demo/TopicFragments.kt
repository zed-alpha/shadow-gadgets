package com.zedalpha.shadowgadgets.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zedalpha.shadowgadgets.clipOutlineShadow


class BasicsFragment : TopicFragment(R.layout.fragment_basics) {
    override val targetIds = intArrayOf(R.id.view_example, R.id.view_drag_drop)

    private var dragDropParentId = R.id.frame_drag_drop_one

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dragDropView = view.findViewById<View>(R.id.view_drag_drop)

        val zac = view.findViewById<ZedAlphaControl>(R.id.zac_basics)
        zac.listener = object : ZedAlphaControl.Listener {
            override fun onElevationChange(exampleView: View, elevation: Float) {
                exampleView.elevation = elevation
                dragDropView.elevation = elevation
            }

            override fun onColorChange(exampleView: View, color: Int) {
                val tint = ColorStateList.valueOf(color)
                exampleView.backgroundTintList = tint
                dragDropView.backgroundTintList = tint
            }
        }

        // Drag and drop
        val frameOne = view.findViewById<FrameLayout>(R.id.frame_drag_drop_one)
        val frameTwo = view.findViewById<FrameLayout>(R.id.frame_drag_drop_two)
        val frameThree = view.findViewById<FrameLayout>(R.id.frame_drag_drop_three)
        val startingId = savedInstanceState?.getInt("drag_drop_parent") ?: dragDropParentId
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
        outState.putInt("drag_drop_parent", parent?.id ?: 0)
    }

    private fun View.scale(scale: Float) {
        scaleX = scale
        scaleY = scale
    }
}

class MotionsFragment : TopicFragment(R.layout.fragment_motions) {
    override val targetIds =
        intArrayOf(R.id.view_motion, R.id.fab_start, R.id.fab_center, R.id.fab_end)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fabStart = view.findViewById<FloatingActionButton>(R.id.fab_start)
        val fabCenter = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_center)
        val fabEnd = view.findViewById<FloatingActionButton>(R.id.fab_end)

        fabStart.setOnClickListener {
            Snackbar.make(it, "Lorem ipsum", Snackbar.LENGTH_SHORT).show()
        }
        fabCenter.setOnClickListener {
            val snack = Snackbar.make(it, "Lorem ipsum", Snackbar.LENGTH_INDEFINITE)
            snack.addCallback(object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar?) {
                    fabStart.hide(); fabCenter.hide(); fabEnd.hide()
                    fabCenter.postDelayed(1000) {
                        fabStart.addOnShowAnimationListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                snack.dismiss()
                            }
                        })
                        fabStart.show(); fabCenter.show(); fabEnd.show()
                    }
                }
            })
            snack.show()
        }
        fabEnd.setOnClickListener {
            fabStart.hide(); fabCenter.hide(); fabEnd.hide()
            fabCenter.postDelayed(1000) {
                fabStart.show(); fabCenter.show(); fabEnd.show()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
class ColorsFragment : TopicFragment(R.layout.fragment_colors) {
    override val targetIds =
        intArrayOf(R.id.view_example, R.id.fab_start, R.id.fab_center, R.id.fab_end)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fabStart = view.findViewById<View>(R.id.fab_start)
        val fabCenter = view.findViewById<View>(R.id.fab_center)
        val fabEnd = view.findViewById<View>(R.id.fab_end)
        view.findViewById<ZedAlphaControl>(R.id.zac_colors).listener =
            object : ZedAlphaControl.Listener {
                override fun onElevationChange(exampleView: View, elevation: Float) {
                    exampleView.elevation = elevation
                }

                override fun onColorChange(exampleView: View, color: Int) {
                    exampleView.outlineShadowColor(color)
                    fabStart.outlineShadowColor(color)
                    fabCenter.outlineShadowColor(color)
                    fabEnd.outlineShadowColor(color)
                }
            }
    }

    private fun View.outlineShadowColor(@ColorInt color: Int) {
        outlineAmbientShadowColor = color
        outlineSpotShadowColor = color
    }
}

class LimitationsFragment : TopicFragment(R.layout.fragment_limitations) {
    override val targetIds =
        intArrayOf(
            R.id.view_limitations_one,
            R.id.view_limitations_two,
            R.id.view_limitations_three,
            R.id.view_circle,
            R.id.view_rectangle,
            R.id.view_rounded_rectangle,
            R.id.view_roundish_rectangle
        )
}

sealed class TopicFragment(layoutResId: Int) : Fragment(layoutResId) {
    abstract val targetIds: IntArray

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setTargetClippingEnabled((parentFragment as ShowcaseFragment).isClippingEnabled)
    }

    fun setTargetClippingEnabled(enabled: Boolean) {
        targetIds.forEach { requireView().findViewById<View>(it).clipOutlineShadow = enabled }
    }
}