package com.zedalpha.shadowgadgets.demo.topic.compat

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentCompatStressTestBinding
import com.zedalpha.shadowgadgets.demo.internal.toColorStateList
import com.zedalpha.shadowgadgets.demo.topic.ClearBlueInt
import com.zedalpha.shadowgadgets.demo.topic.ClearGreenInt
import com.zedalpha.shadowgadgets.demo.topic.ClearRedInt
import com.zedalpha.shadowgadgets.demo.topic.ColorfulHolder
import com.zedalpha.shadowgadgets.demo.topic.ColorfulLazyColumn
import com.zedalpha.shadowgadgets.demo.topic.HalfItemCount
import com.zedalpha.shadowgadgets.demo.topic.ItemCount
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane

internal val CompatStressTestTopic =
    Topic(
        title = "Compat - Stress Test",
        descriptionResId = R.string.description_compat_stress_test,
        fragmentClass = CompatStressTestFragment::class.java
    )

class CompatStressTestFragment : TopicFragment<FragmentCompatStressTestBinding>(
    FragmentCompatStressTestBinding::inflate
) {
    override fun loadUi(ui: FragmentCompatStressTestBinding) {
        ui.recycler.adapter = VeryColorfulAdapter()
        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { ComposeContent() }
        }
    }
}

private class VeryColorfulAdapter : RecyclerView.Adapter<ColorfulHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun getItemCount() = ItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ColorfulHolder(parent).also { holder ->
            holder.itemView.forceOutlineShadowColorCompat = true
        }

    override fun onBindViewHolder(holder: ColorfulHolder, position: Int) {
        val plane =
            when (position % 3) {
                0 -> ShadowPlane.Foreground
                1 -> ShadowPlane.Background
                else -> ShadowPlane.Inline
            }
        val color =
            evaluator.evaluate(
                (position % HalfItemCount).toFloat() / HalfItemCount,
                if (position < HalfItemCount) ClearRedInt else ClearGreenInt,
                if (position < HalfItemCount) ClearGreenInt else ClearBlueInt
            ) as Int
        holder.itemView.apply {
            shadowPlane = plane
            backgroundTintList = color.toColorStateList()
            outlineShadowColorCompat = ColorUtils.setAlphaComponent(color, 255)
        }
        @SuppressLint("SetTextI18n")
        holder.text.text = "Item $position"
    }
}

@Composable
private fun ComposeContent() {
    ColorfulLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 20.dp),
        enableColorCompat = true
    )
}