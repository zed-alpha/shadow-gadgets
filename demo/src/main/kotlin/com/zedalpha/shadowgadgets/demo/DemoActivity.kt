package com.zedalpha.shadowgadgets.demo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zedalpha.shadowgadgets.demo.databinding.ActivityDemoBinding
import com.zedalpha.shadowgadgets.demo.internal.applyInsetsListener
import com.zedalpha.shadowgadgets.demo.internal.inflateUnattached
import com.zedalpha.shadowgadgets.demo.internal.showWelcomeDialog
import com.zedalpha.shadowgadgets.demo.topic.ColorCompatDrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.ColorCompatIntroTopic
import com.zedalpha.shadowgadgets.demo.topic.ColorCompatStressTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeDropTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeIntroTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeLambdaTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeRootTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewDrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewGroupsTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewIntroTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewIrregularTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewMotionTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewPlaneTopic
import com.zedalpha.shadowgadgets.demo.topic.ViewRootTopic

class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ui = ActivityDemoBinding.inflate(layoutInflater)
        ui.root.applyInsetsListener()
        setContentView(ui.root)

        ui.contentPager.apply {
            adapter = ContentAdapter(this@DemoActivity)
            isUserInputEnabled = false
        }
        ui.infoPager.apply {
            adapter = InfoAdapter()
            isUserInputEnabled = false
        }

        var current = -1

        fun setTopic(request: Int) {
            val index = request.coerceIn(Topics.indices)
            if (current == index) return

            ui.infoPager.currentItem = index
            ui.contentPager.currentItem = index
            ui.title.apply {
                setDirection(index > current)
                setText(Topics[index].title)
            }
            ui.backward.isInvisible = index == 0
            ui.forward.isInvisible = index == Topics.lastIndex

            current = index
        }

        ui.backward.setOnClickListener { setTopic(current - 1) }
        ui.forward.setOnClickListener { setTopic(current + 1) }

        ui.title.setOnClickListener { title ->
            PopupMenu(this, title).run {
                Topics.forEachIndexed { i, t -> menu.add(0, i, 0, t.title) }
                setOnMenuItemClickListener { setTopic(it.itemId); true }
                menu[current].isEnabled = false
                show()
            }
        }

        ui.root.post { setTopic(ui.contentPager.currentItem) }

        if (savedInstanceState == null) showWelcomeDialog()
    }
}

private val Topics =
    listOf(
        ViewIntroTopic,
        ViewMotionTopic,
        ViewPlaneTopic,
        ViewGroupsTopic,
        ViewIrregularTopic,
        ViewDrawableTopic,
        ViewRootTopic,
        ComposeIntroTopic,
        ComposeLambdaTopic,
        ComposeDropTopic,
        ComposeRootTopic,
        ColorCompatIntroTopic,
        ColorCompatDrawableTopic,
        ColorCompatStressTopic,
    )

private class ContentAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount() = Topics.size

    override fun createFragment(position: Int) =
        Topics[position].createFragment()
}

private class InfoAdapter : RecyclerView.Adapter<InfoHolder>() {

    override fun getItemCount() = Topics.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InfoHolder(parent)

    override fun onBindViewHolder(holder: InfoHolder, position: Int) =
        holder.bind(position)
}

private class InfoHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(parent.inflateUnattached(R.layout.item_description)) {

    private val text: TextView = itemView.findViewById(R.id.text)

    init {
        text.setOnLongClickListener {
            AlertDialog.Builder(it.context)
                .setView(R.layout.dialog_description)
                .setPositiveButton("Close", null)
                .show()
                .findViewById<TextView>(R.id.text)?.text = text.text
            true
        }
    }

    fun bind(position: Int) {
        text.text = Topics[position].createDescription(itemView.context)
    }
}