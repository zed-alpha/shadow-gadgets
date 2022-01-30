package com.zedalpha.shadowgadgets.demo

import android.app.Activity
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

        findViewById<View>(R.id.root).background = PlatformDrawable()
    }
}

class CompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compat)

        findViewById<View>(R.id.root).background = CompatDrawable()
    }
}

class MaterialComponentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        attachMaterialComponentsShadowHelper(
            listOf(
                idMatcher(matchName = "_shadow", matchRule = MatchRule.EndsWith)
            )
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_components)

        findViewById<View>(R.id.root).background = MaterialDrawable()
    }
}