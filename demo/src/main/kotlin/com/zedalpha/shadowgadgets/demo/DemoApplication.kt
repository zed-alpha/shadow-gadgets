package com.zedalpha.shadowgadgets.demo

import android.app.Application
import com.zedalpha.shadowgadgets.view.ShadowGadgets as ViewShadowGadgets

class DemoApplication : Application() {

    init {
        // The alias is to emphasize that this does not apply to :compose.
        ViewShadowGadgets.throwOnUnhandledErrors = true

        // Uncomment the following if you'd like to see the View: Root
        // topic fallback without having to run an old Android version.
        // Everything else should look and work exactly the same. If
        // you notice any discrepancies, please file a bug report.
        // ViewShadowGadgets.forceFallbackDrawMethod = true
    }
}