package com.zedalpha.shadowgadgets.demo.topic

import android.graphics.Path
import android.os.Build
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentIrregularBinding
import com.zedalpha.shadowgadgets.view.MaterialComponentsViewPathProvider
import com.zedalpha.shadowgadgets.view.ViewPathProvider
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.pathProvider

internal val IrregularTopic = Topic(
    "Irregular",
    R.string.description_irregular,
    IrregularFragment::class.java
)

class IrregularFragment : TopicFragment<FragmentIrregularBinding>(
    FragmentIrregularBinding::inflate
) {
    override fun loadUi(ui: FragmentIrregularBinding) {
        if (Build.VERSION.SDK_INT < 30) {
            ui.caveat.text = getString(
                R.string.message_irregular,
                Build.VERSION.SDK_INT
            )
            ui.labelBroken.isInvisible = true
            ui.labelFixed.isInvisible = true
            ui.caveat.isVisible = true
        }

        ui.viewFixed.pathProvider = ViewPathProvider { v, p ->
            val side = v.width.toFloat()
            p.addRoundRect(
                0F, 0F,
                side, side,
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