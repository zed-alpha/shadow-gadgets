package com.zedalpha.shadowgadgets.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zedalpha.shadowgadgets.demo.databinding.ActivityMainBinding
import com.zedalpha.shadowgadgets.demo.topic.ApplyTopic
import com.zedalpha.shadowgadgets.demo.topic.ComposeTopic
import com.zedalpha.shadowgadgets.demo.topic.DrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.IntroTopic
import com.zedalpha.shadowgadgets.demo.topic.IrregularTopic
import com.zedalpha.shadowgadgets.demo.topic.MotionTopic
import com.zedalpha.shadowgadgets.demo.topic.PlaneTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatDrawableTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatIntroTopic
import com.zedalpha.shadowgadgets.demo.topic.compat.CompatStressTestTopic

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.contentPager.apply {
            adapter = ContentAdapter(this@MainActivity)
            isUserInputEnabled = false
        }

        ui.infoPager.apply {
            adapter = InfoAdapter()
            isUserInputEnabled = false
        }

        var current = 0
        ui.title.setText(topics[0].title)

        fun setTopic(index: Int) {
            if (current == index) return
            ui.infoPager.currentItem = index
            ui.contentPager.currentItem = index
            ui.title.apply {
                setDirection(index > current)
                setText(topics[index].title)
            }
            ui.backward.isInvisible = index == 0
            ui.forward.isInvisible = index == topics.size - 1
            current = index
        }

        ui.backward.setOnClickListener {
            if (current > 0) setTopic(current - 1)
        }
        ui.forward.setOnClickListener {
            if (current < topics.size - 1) setTopic(current + 1)
        }
        ui.title.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                topics.forEachIndexed { i, t -> menu.add(0, i, 0, t.title) }
                menu.getItem(current).isEnabled = false
                setOnMenuItemClickListener { setTopic(it.itemId); true }
            }.show()
        }

        if (savedInstanceState == null) {
            val hideWelcome = getPreferences(Context.MODE_PRIVATE)
                .getBoolean(PREF_HIDE_WELCOME, false)
            if (!hideWelcome) {
                val dialog = AlertDialog.Builder(this)
                    .setView(R.layout.dialog_welcome)
                    .setPositiveButton("Close", null)
                    .show()
                val checkBox = dialog.findViewById<CheckBox>(R.id.hide_welcome)
                checkBox?.setOnCheckedChangeListener { _, isChecked ->
                    getPreferences(Context.MODE_PRIVATE).edit()
                        .putBoolean(PREF_HIDE_WELCOME, isChecked)
                        .apply()
                }
            }
        }
    }
}

private val topics = listOf(
    IntroTopic,
    MotionTopic,
    PlaneTopic,
    ApplyTopic,
    IrregularTopic,
    DrawableTopic,
    ComposeTopic,
    CompatIntroTopic,
    CompatDrawableTopic,
    CompatStressTestTopic
)

private class ContentAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount() = topics.size

    override fun createFragment(position: Int) =
        topics[position].createFragment()
}

private class InfoAdapter : RecyclerView.Adapter<InfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InfoHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_description, parent, false)
        )

    override fun getItemCount() = topics.size

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.text.setText(topics[position].descriptionResId)
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

private const val PREF_HIDE_WELCOME = "hide_welcome"