package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Path
import android.view.View
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentIrregularBinding
import com.zedalpha.shadowgadgets.view.ViewPathProvider
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.pathProvider


internal object IrregularTopic : Topic {

    override val title = "Irregular"

    override val descriptionResId = R.string.description_irregular

    override fun createContentFragment() = Content()

    class Content : ContentFragment(R.layout.fragment_irregular) {

        override fun loadUi(view: View) {
            val ui = FragmentIrregularBinding.bind(view)

            ui.fixed.pathProvider = ViewPathProvider { v, p ->
                val side = v.width.toFloat()
                p.addRoundRect(
                    0F,
                    0F,
                    side,
                    side,
                    floatArrayOf(side / 2, side / 2, 0F, 0F, 0F, 0F, 0F, 0F),
                    Path.Direction.CW
                )
            }

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.broken.clipOutlineShadow = isChecked
                ui.fixed.clipOutlineShadow = isChecked
            }
        }
    }
}