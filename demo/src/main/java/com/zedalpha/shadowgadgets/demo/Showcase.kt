package com.zedalpha.shadowgadgets.demo

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial


@SuppressLint("NewApi")
private val Topics = listOf(
    Topic("Basics", BasicsFragment::class.java, Build.VERSION_CODES.LOLLIPOP),
    Topic("Motions", MotionsFragment::class.java, Build.VERSION_CODES.LOLLIPOP),
    Topic("Colors", ColorsFragment::class.java, Build.VERSION_CODES.P),
    Topic("Limitations", LimitationsFragment::class.java, Build.VERSION_CODES.LOLLIPOP)
)

private class Topic(val title: String, val fragmentClass: Class<out TopicFragment>, minSdk: Int) {
    val isAvailable = Build.VERSION.SDK_INT >= minSdk

    fun createFragment(): Fragment =
        if (isAvailable) fragmentClass.newInstance() else UnavailableFragment()

    override fun toString() = title
}

class UnavailableFragment : TopicFragment(R.layout.fragment_unavailable) {
    override val targetIds = intArrayOf()
}

class ShowcaseFragment : Fragment(R.layout.fragment_showcase) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.background = SlantGridDrawable(0xFFF8F8F8.toInt(), 0xFFDDDDDD.toInt())

        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter(view.context, R.layout.spinner_item, Topics)
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

        view.findViewById<SwitchMaterial>(R.id.switch_clip)
            .setOnCheckedChangeListener { _, isChecked ->
                displayedFragment?.setTargetClippingEnabled(isChecked)
            }

        if (savedInstanceState == null) setDisplayedFragment(Topics[0])
    }

    val isClippingEnabled: Boolean
        get() = requireView().findViewById<SwitchMaterial>(R.id.switch_clip).isChecked

    private fun setDisplayedFragment(topic: Topic) {
        val manager = childFragmentManager
        val current = displayedFragment
        if (current?.tag != topic.title) {
            val transaction = manager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.fade_out
            )
            if (current != null) transaction.detach(current)
            val next = manager.findFragmentByTag(topic.title)
            if (next == null) {
                transaction.add(R.id.topic_container, topic.createFragment(), topic.title)
            } else {
                transaction.attach(next)
            }
            transaction.commit()
        }
    }

    private val displayedFragment: TopicFragment?
        get() = childFragmentManager.findFragmentById(R.id.topic_container) as? TopicFragment
}