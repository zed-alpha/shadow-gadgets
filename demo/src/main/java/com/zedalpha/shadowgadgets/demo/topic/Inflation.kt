package com.zedalpha.shadowgadgets.demo.topic

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zedalpha.shadowgadgets.demo.CompatDrawable
import com.zedalpha.shadowgadgets.demo.MaterialDrawable
import com.zedalpha.shadowgadgets.demo.PlatformDrawable
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.inflation.MatchRule
import com.zedalpha.shadowgadgets.inflation.attachMaterialComponentsShadowHelper
import com.zedalpha.shadowgadgets.inflation.attachShadowHelper
import com.zedalpha.shadowgadgets.inflation.idMatcher
import kotlin.reflect.KClass


class InflationFragment : TopicFragment(R.layout.fragment_inflation) {
    override val targetIds = intArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

class PlatformActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachShadowHelper()
        setContentView(R.layout.activity_platform)

        setRootBackground(PlatformDrawable())
    }
}

class CompatActivity : AppCompatActivity(R.layout.activity_compat) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRootBackground(CompatDrawable())
    }
}

class MaterialComponentsActivity : AppCompatActivity(R.layout.activity_material_components) {
    override fun onCreate(savedInstanceState: Bundle?) {
        attachMaterialComponentsShadowHelper(
            listOf(
                idMatcher(matchName = "_shadow", matchRule = MatchRule.EndsWith)
            )
        )
        super.onCreate(savedInstanceState)

        setRootBackground(MaterialDrawable())
    }
}

internal fun Activity.setRootBackground(drawable: Drawable?) {
    findViewById<View>(R.id.root).background = drawable
}