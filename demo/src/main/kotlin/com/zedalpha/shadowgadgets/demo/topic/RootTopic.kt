package com.zedalpha.shadowgadgets.demo.topic

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.ShapeDrawable
import android.util.Size
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.DialogRootBinding
import com.zedalpha.shadowgadgets.demo.databinding.FragmentRootBinding
import com.zedalpha.shadowgadgets.demo.databinding.ViewRootBinding
import com.zedalpha.shadowgadgets.view.ExperimentalShadowGadgets
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.onShadowAttach
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane

internal val RootTopic =
    Topic(
        title = "Root",
        descriptionResId = R.string.description_root,
        fragmentClass = RootFragment::class.java
    )

class RootFragment :
    TopicFragment<FragmentRootBinding>(FragmentRootBinding::inflate) {

    override fun loadUi(ui: FragmentRootBinding) {
        ui.buttonDialog.setOnClickListener { showDialog() }
        ui.buttonView.setOnClickListener { toggleView() }
    }

    private fun showDialog() {
        val activity = requireActivity()
        val dialog =
            Dialog(activity, R.style.Theme_ShadowGadgets_Demo_Dialog).apply {
                val dui = DialogRootBinding.inflate(activity.layoutInflater)
                setContentView(dui.root)
                dui.text.setOnClickListener { dismiss() }
                setCanceledOnTouchOutside(true)
            }
        val window = dialog.window!!

        window.decorView.apply {
            elevation = 10 * requireContext().resources.displayMetrics.density
            rotation = -2F
            clipToOutline = false
            outlineProvider = AlphaFixProvider
            shadowPlane = ShadowPlane.Inline
            clipOutlineShadow = true
            outlineShadowColorCompat = Color.BLUE
            forceOutlineShadowColorCompat = true
        }

        val size = popupSize()
        val location = popupLocation()
        window.attributes =
            window.attributes!!.apply {
                @SuppressLint("RtlHardcoded")
                gravity = Gravity.TOP or Gravity.LEFT
                width = size.width
                height = size.height
                x = location.x
                y = location.y
            }

        dialog.show()
    }

    private val textView by lazy {
        val vui = ViewRootBinding.inflate(requireActivity().layoutInflater)
        vui.root.apply {
            rotation = 2F
            outlineProvider = AlphaFixProvider
            shadowPlane = ShadowPlane.Inline
            clipOutlineShadow = true
            outlineShadowColorCompat = Color.BLUE
            forceOutlineShadowColorCompat = true
            setOnClickListener { toggleView() }

            @SuppressLint("ClickableViewAccessibility")
            setOnTouchListener { _, event ->
                val isOutside = event.action == MotionEvent.ACTION_OUTSIDE
                if (isOutside) toggleView()
                isOutside
            }

            @OptIn(ExperimentalShadowGadgets::class)
            onShadowAttach { isDrawn ->
                if (isDrawn) return@onShadowAttach
                foreground =
                    ShapeDrawable().apply {
                        paint.color = Color.MAGENTA
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 5F
                    }
            }
        }
    }

    private val viewParams =
        WindowManager.LayoutParams(
            0, 0,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            @SuppressLint("RtlHardcoded")
            gravity = Gravity.TOP or Gravity.LEFT
        }

    private fun popupSize() =
        ui.root.run { Size((0.9F * width).toInt(), (0.9F * height).toInt()) }

    private fun popupLocation() =
        ui.root.run {
            val location = IntArray(2)
            getLocationOnScreen(location)
            val x = location[0] + (0.05F * width).toInt()
            val y = location[1] + (0.05F * height).toInt()
            Point(x, y)
        }

    private fun toggleView() {
        val view = textView
        val manager = requireActivity().windowManager

        if (view.tag == null) {
            view.tag = manager

            val size = popupSize()
            val location = popupLocation()
            val yOffset = Resources.getSystem().run {
                @SuppressLint("DiscouragedApi,InternalInsetResource")
                val id = getIdentifier("status_bar_height", "dimen", "android")
                if (id != 0) getDimensionPixelSize(id) else 0
            }
            val params = viewParams.apply {
                width = size.width
                height = size.height
                x = location.x
                y = location.y - yOffset
            }
            manager.addView(view, params)
        } else {
            view.tag = null

            manager.removeView(view)
        }
    }
}

private val AlphaFixProvider =
    object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            BACKGROUND.getOutline(view, outline)
            outline.alpha = 1F
        }
    }