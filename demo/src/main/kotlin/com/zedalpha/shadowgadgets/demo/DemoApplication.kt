package com.zedalpha.shadowgadgets.demo

import android.app.Application
import com.zedalpha.shadowgadgets.view.ShadowGadgets as ViewShadowGadgets

class DemoApplication : Application() {

    init {
        // The alias is to clarify that this does not affect :compose.
        ViewShadowGadgets.throwOnUnhandledErrors = true

        // Uncomment the following if you'd like to see the View: Root
        // topic fallback without having to run an old Android version.
        // Everything else should look and work exactly as normal.
        // ViewShadowGadgets.forceFallbackDrawMethod = true
    }
}