package com.zedalpha.shadowgadgets.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass


class InflationFragment : Fragment(R.layout.fragment_inflation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setStart(view, R.id.button_platform, PlatformActivity::class)
        setStart(view, R.id.button_appcompat, CompatActivity::class)
        setStart(view, R.id.button_material, MaterialComponentsActivity::class)
    }

    private fun setStart(view: View, id: Int, activity: KClass<out Activity>) {
        view.findViewById<View>(id).setOnClickListener {
            startActivity(Intent(requireContext(), activity.java))
        }
    }
}