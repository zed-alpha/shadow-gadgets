package com.zedalpha.shadowgadgets.demo.topic

import android.animation.ArgbEvaluator
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.compose.clippedShadow
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentColorCompatStressBinding
import com.zedalpha.shadowgadgets.demo.internal.ItemElevation
import com.zedalpha.shadowgadgets.demo.internal.ItemShape
import com.zedalpha.shadowgadgets.demo.internal.setTopicContent
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.updateShadow

internal val ColorCompatStressTopic =
    Topic(
        title = "ColorCompat: Stress",
        descriptionResId = R.string.description_color_compat_stress,
        fragmentClass = ColorCompatStressFragment::class.java
    )

class ColorCompatStressFragment :
    TopicFragment<FragmentColorCompatStressBinding>(
        inflate = FragmentColorCompatStressBinding::inflate
    ) {

    override fun loadUi(ui: FragmentColorCompatStressBinding) {
        ui.recycler.adapter = VeryColorfulAdapter()
        ui.composeView.setTopicContent { ComposeStressTestContent() }
    }
}

private class VeryColorfulAdapter : RecyclerView.Adapter<VeryColorfulHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun getItemCount() = ItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VeryColorfulHolder(parent, evaluator)

    override fun onBindViewHolder(holder: VeryColorfulHolder, position: Int) =
        holder.bind(position)
}

private class VeryColorfulHolder(parent: ViewGroup, evaluator: ArgbEvaluator) :
    ColorfulHolder(parent, evaluator) {

    init {
        itemView.forceOutlineShadowColorCompat = true
    }

    override fun bind(position: Int) {
        super.bind(position)
        itemView.updateShadow {
            outlineShadowColorCompat = ColorUtils.setAlphaComponent(color, 255)
            shadowPlane = ShadowPlane.entries[position % 3]
        }
    }
}

@Composable
private fun ComposeStressTestContent() {
    ColorfulLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 20.dp),
        shadowModifier = { color ->
            clippedShadow(ItemShape) {
                elevation = ItemElevation.toPx()
                colorCompat = color
                forceColorCompat = true
            }
        }
    )
}