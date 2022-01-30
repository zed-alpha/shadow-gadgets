package com.zedalpha.shadowgadgets.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.zedalpha.shadowgadgets.clipOutlineShadow
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.root).background = MainDrawable()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val content = findViewById<ViewGroup>(R.id.content)
        val toggle = findViewById<SwitchMaterial>(R.id.switch_clip_local)

        toggle.setOnCheckedChangeListener { _, isChecked ->
            toolbar.clipOutlineShadow = isChecked
            for (index in 0 until content.childCount) {
                content.getChildAt(index).clipOutlineShadow = isChecked
            }
            toggle.clipOutlineShadow = isChecked
        }

        setStart(R.id.button_platform, PlatformActivity::class)
        setStart(R.id.button_appcompat, CompatActivity::class)
        setStart(R.id.button_material, MaterialComponentsActivity::class)
    }

    private fun setStart(id: Int, activity: KClass<out Activity>) {
        findViewById<View>(id).setOnClickListener { startActivity(Intent(this, activity.java)) }
    }
}