package com.zedalpha.shadowgadgets.demo.topic.compat

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
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
import com.zedalpha.shadowgadgets.demo.topic.ColorfulHolder
import com.zedalpha.shadowgadgets.demo.topic.ColorfulLazyColumn
import com.zedalpha.shadowgadgets.demo.topic.HALF_ITEM_COUNT
import com.zedalpha.shadowgadgets.demo.topic.ITEM_BLUE
import com.zedalpha.shadowgadgets.demo.topic.ITEM_COUNT
import com.zedalpha.shadowgadgets.demo.topic.ITEM_GREEN
import com.zedalpha.shadowgadgets.demo.topic.ITEM_RED
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane

internal val CompatStressTestTopic = Topic(
    "Compat - Stress Test",
    R.string.description_compat_stress_test,
    CompatStressTestFragment::class.java
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_colorful, parent, false).let { view ->
                view.forceOutlineShadowColorCompat = true
                ColorfulHolder(view)
            }

    override fun onBindViewHolder(holder: ColorfulHolder, position: Int) {
        val color = evaluator.evaluate(
            (position % HALF_ITEM_COUNT).toFloat() / HALF_ITEM_COUNT,
            if (position < HALF_ITEM_COUNT) ITEM_RED else ITEM_GREEN,
            if (position < HALF_ITEM_COUNT) ITEM_GREEN else ITEM_BLUE
        ) as Int
        holder.itemView.apply {
            backgroundTintList = ColorStateList.valueOf(color)
            outlineShadowColorCompat = ColorUtils.setAlphaComponent(color, 255)
            shadowPlane = when (position % 3) {
                0 -> ShadowPlane.Foreground
                1 -> ShadowPlane.Background
                else -> ShadowPlane.Inline
            }
        }
        @SuppressLint("SetTextI18n")
        holder.textView.text = "Item $position"
    }

    override fun getItemCount() = ITEM_COUNT
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