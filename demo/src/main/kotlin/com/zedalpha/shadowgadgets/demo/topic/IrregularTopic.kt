package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Path
import android.os.Build
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentIrregularBinding
import com.zedalpha.shadowgadgets.view.MaterialComponentsViewPathProvider
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

            if (Build.VERSION.SDK_INT < 30) {
                ui.caveat.text =
                    getString(R.string.caveat_irregular, Build.VERSION.SDK_INT)
                ui.labelBroken.isInvisible = true
                ui.labelFixed.isInvisible = true
                ui.caveat.isVisible = true
            }

            ui.viewFixed.pathProvider = ViewPathProvider { v, p ->
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

            ui.buttonFixed.pathProvider = MaterialComponentsViewPathProvider

            ui.clipSwitch.setOnCheckedChangeListener { _, isChecked ->
                ui.viewBroken.clipOutlineShadow = isChecked
                ui.viewFixed.clipOutlineShadow = isChecked
                ui.buttonBroken.clipOutlineShadow = isChecked
                ui.buttonFixed.clipOutlineShadow = isChecked
            }
        }
    }
}