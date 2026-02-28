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
import com.zedalpha.shadowgadgets.demo.topic.ColorfulLazyColumn
import com.zedalpha.shadowgadgets.demo.topic.HalfItemCount
import com.zedalpha.shadowgadgets.demo.topic.ItemCount
import com.zedalpha.shadowgadgets.demo.topic.ItemHolder
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import com.zedalpha.shadowgadgets.view.ExperimentalShadowGadgets
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.updateShadow

internal val CompatStressTestTopic =
    Topic(
        title = "Compat - Stress Test",
        descriptionResId = R.string.description_compat_stress_test,
        fragmentClass = CompatStressTestFragment::class.java
    )

class CompatStressTestFragment :
    TopicFragment<FragmentCompatStressTestBinding>(
        inflate = FragmentCompatStressTestBinding::inflate
    ) {

    override fun loadUi(ui: FragmentCompatStressTestBinding) {
        ui.recycler.adapter = VeryColorfulAdapter()
        ui.composeView.apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent { ComposeStressTestContent() }
        }
    }
}

private class VeryColorfulAdapter : RecyclerView.Adapter<ItemHolder>() {

    private val evaluator = ArgbEvaluator()

    override fun getItemCount() = ItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(parent).also { holder ->
            holder.itemView.forceOutlineShadowColorCompat = true
        }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val color =
            evaluator.evaluate(
                (position % HalfItemCount).toFloat() / HalfItemCount,
                if (position < HalfItemCount) ClearRedInt else ClearGreenInt,
                if (position < HalfItemCount) ClearGreenInt else ClearBlueInt
            ) as Int

        @OptIn(ExperimentalShadowGadgets::class)
        holder.itemView.updateShadow {
            backgroundTintList = color.toColorStateList()
            outlineShadowColorCompat = ColorUtils.setAlphaComponent(color, 255)
            shadowPlane = ShadowPlane.entries[position % 3]
        }

        @SuppressLint("SetTextI18n")
        holder.text.text = "Item $position"
    }
}

@Composable
private fun ComposeStressTestContent() {
    ColorfulLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 20.dp),
        enableColorCompat = true
    )
}