package com.zedalpha.shadowgadgets.demo

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zedalpha.shadowgadgets.inflation.MatchRule
import com.zedalpha.shadowgadgets.inflation.attachMaterialComponentsShadowHelper
import com.zedalpha.shadowgadgets.inflation.attachShadowHelper
import com.zedalpha.shadowgadgets.inflation.idMatcher


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