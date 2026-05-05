package com.zedalpha.shadowgadgets.demo

import android.app.Application
import com.zedalpha.shadowgadgets.view.ShadowGadgets as ViewShadowGadgets

class DemoApplication : Application() {

    init {
        // These settings do not apply to :compose, hence the alias.
        ViewShadowGadgets.throwOnUnhandledErrors = true

        // An easy way to test the fallback possible on API levels 21..28.
        // ViewShadowGadgets.forceFallbackDrawMethod = true
    }
}