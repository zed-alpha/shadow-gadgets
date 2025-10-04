package com.zedalpha.shadowgadgets.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zedalpha.shadowgadgets.demo.databinding.ActivityMainBinding
import com.zedalpha.shadowgadgets.demo.internal.RoundedCornerViewOutlineProvider
import com.zedalpha.shadowgadgets.demo.internal.applyInsetsListener
import com.zedalpha.shadowgadgets.demo.internal.showWelcomeDialog
import com.zedalpha.shadowgadgets.demo.topic.ApplyTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeTopic
import com.zedalpha.shadowgadgets.demo.topic.DrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.IntroTopic
import com.zedalpha.shadowgadgets.demo.topic.IrregularTopic
import com.zedalpha.shadowgadgets.demo.topic.MotionTopic
import com.zedalpha.shadowgadgets.demo.topic.PlaneTopic
import com.zedalpha.shadowgadgets.demo.topic.RootTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatDrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatIntroTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatStressTestTopic

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ui = ActivityMainBinding.inflate(layoutInflater)
        ui.root.applyInsetsListener()
        setContentView(ui.root)

        ui.contentPager.apply {
            adapter = ContentAdapter(this@MainActivity)
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
            PopupMenu(this, title).apply {
                Topics.forEachIndexed { i, t -> menu.add(0, i, 0, t.title) }
                menu[current].isEnabled = false
                setOnMenuItemClickListener { setTopic(it.itemId); true }
            }.show()
        }

        setTopic(0)

        if (savedInstanceState == null) showWelcomeDialog()
    }
}

private val Topics =
    listOf(
        IntroTopic,
        MotionTopic,
        PlaneTopic,
        ApplyTopic,
        IrregularTopic,
        DrawableTopic,
        RootTopic,
        ComposeTopic,
        CompatIntroTopic,
        CompatDrawableTopic,
        CompatStressTestTopic,
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
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_description, parent, false).run {
                outlineProvider = RoundedCornerViewOutlineProvider()
                clipToOutline = true
                InfoHolder(this)
            }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.text.setText(Topics[position].descriptionResId)
    }
}

private class InfoHolder(view: View) : ViewHolder(view) {

    val text: TextView = view.findViewById(R.id.text)

    init {
        text.setOnLongClickListener {
            AlertDialog.Builder(text.context)
                .setView(R.layout.dialog_description)
                .setPositiveButton("Close", null)
                .show()
                .findViewById<TextView>(R.id.text)?.text = text.text
            true
        }
    }
}