package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater

/**
 * Attaches the platform layout inflation helper.
 *
 * Must be called before `setContentView()`.
 */
fun Activity.attachShadowHelper() {
    layoutInflater.factory = PlatformShadowHelper(this)
}

private class PlatformShadowHelper(context: Context) : LayoutInflater.Factory {

    private val helper = InflationHelper(context)

    override fun onCreateView(
        name: String,
        context: Context,
        attrs: AttributeSet
    ) = helper.processTag(name, context, attrs)
}

/**
 * A simple [Application] subclass that unconditionally sets the platform shadow
 * inflater helper on every [Activity] instance that is created. Included mainly
 * for illustrative purposes.
 */
class PlatformShadowHelperApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(
            ActivityCreatedCallback { activity, _ ->
                activity.attachShadowHelper()
            }
        )
    }
}

/**
 * This is an adapter interface with empty defaults for all of
 * [Application.ActivityLifecycleCallbacks]' functions except the one.
 */
fun interface ActivityCreatedCallback : Application.ActivityLifecycleCallbacks {

    // Overridden for dokka
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    )

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }
}