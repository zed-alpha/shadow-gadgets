package com.zedalpha.shadowgadgets.demo.topic

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.demo.databinding.FragmentInflationBinding
import com.zedalpha.shadowgadgets.demo.internal.CompatDrawable
import com.zedalpha.shadowgadgets.demo.internal.MaterialDrawable
import com.zedalpha.shadowgadgets.demo.internal.PlatformDrawable
import com.zedalpha.shadowgadgets.view.inflation.attachMaterialComponentsShadowHelper
import com.zedalpha.shadowgadgets.view.inflation.attachShadowHelper

@Suppress("unused")
internal val InflationTopic = Topic(
    "Inflation",
    R.string.description_inflation,
    InflationFragment::class.java
)

class InflationFragment : TopicFragment<FragmentInflationBinding>(
    FragmentInflationBinding::inflate
) {
    override fun loadUi(ui: FragmentInflationBinding) {
        ui.buttonPlatform.starts(PlatformActivity::class.java)
        ui.buttonAppcompat.starts(CompatActivity::class.java)
        ui.buttonMaterial.starts(ComponentsActivity::class.java)
    }

    private fun View.starts(activity: Class<out Activity>) {
        setOnClickListener { startActivity(Intent(requireContext(), activity)) }
    }
}

class ComponentsActivity : AppCompatActivity(R.layout.activity_components) {

    override fun onCreate(savedInstanceState: Bundle?) {
        attachMaterialComponentsShadowHelper()
        super.onCreate(savedInstanceState)
        setRootBackground(MaterialDrawable())
    }
}

class CompatActivity : AppCompatActivity(R.layout.activity_compat) {

    override fun onCreate(savedInstanceState: Bundle?) {
        // CompatActivity's ShadowHelper is set from the
        // viewInflaterClass attribute in Theme.Compat.
        super.onCreate(savedInstanceState)
        setRootBackground(CompatDrawable())
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

internal fun Activity.setRootBackground(drawable: Drawable?) {
    findViewById<View>(R.id.root).background = drawable
}