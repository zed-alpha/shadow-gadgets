package com.zedalpha.shadowgadgets.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.switchmaterial.SwitchMaterial
import com.zedalpha.shadowgadgets.demo.topic.Topic
import com.zedalpha.shadowgadgets.demo.topic.TopicFragment
import com.zedalpha.shadowgadgets.demo.topic.Topics
import com.zedalpha.shadowgadgets.demo.topic.setRootBackground


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val switch by lazy { findViewById<SwitchMaterial>(R.id.switch_overlays) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRootBackground(SlantGridDrawable(0xFFEBEBEB.toInt(), 0xFFCECECE.toInt()))

        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, Topics)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setDisplayedFragment(Topics[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        spinner.post { spinner.dropDownWidth = spinner.width }

        switch.setOnCheckedChangeListener { _, isChecked ->
            displayedFragment?.setTargetClippingEnabled(isChecked)
        }

        if (savedInstanceState == null) {
            if (!getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_HIDE_WELCOME, false)) {
                val dialog = AlertDialog.Builder(this)
                    .setView(R.layout.dialog_welcome)
                    .setPositiveButton("Close", null)
                    .show()
                val check = dialog.findViewById<CheckBox>(R.id.check_hide_welcome)
                check?.setOnCheckedChangeListener { _, isChecked ->
                    getPreferences(Context.MODE_PRIVATE).edit()
                        .putBoolean(PREF_HIDE_WELCOME, isChecked)
                        .apply()
                }
            }
            setDisplayedFragment(Topics[0])
        } else {
            switch.isVisible = displayedFragment?.shouldShowToggle == true
        }
    }

    private fun setDisplayedFragment(topic: Topic) {
        val manager = supportFragmentManager
        val current = displayedFragment
        if (current?.tag != topic.title) {
            val transaction = manager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out)
            if (current != null) transaction.detach(current)
            val next =
                manager.findFragmentByTag(topic.title) as? TopicFragment ?: topic.createFragment()
            if (next.isDetached) {
                transaction.attach(next)
            } else {
                transaction.add(R.id.main_content, next, topic.title)
            }
            transaction.commit()
            switch.isVisible = next.shouldShowToggle
        }
    }

    val isClippingEnabled: Boolean
        get() = switch.isChecked

    private val displayedFragment: TopicFragment?
        get() = supportFragmentManager.findFragmentById(R.id.main_content) as? TopicFragment
}

private const val PREF_HIDE_WELCOME = "hide_welcome"