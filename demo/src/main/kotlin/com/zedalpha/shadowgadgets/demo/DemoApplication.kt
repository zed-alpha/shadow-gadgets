package com.zedalpha.shadowgadgets.demo

import android.app.Application
import com.zedalpha.shadowgadgets.view.ExperimentalShadowGadgets
import com.zedalpha.shadowgadgets.view.ShadowGadgets

class DemoApplication : Application() {

    init {
        @OptIn(ExperimentalShadowGadgets::class)
        ShadowGadgets.throwOnUnhandledErrors = true
    }
}