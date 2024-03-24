package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.XmlRes

/**
 * Attaches the platform helper, and searches the theme and manifest for the
 * (optional) matchers XML reference. Must be called before setContentView.
 */
fun Activity.attachShadowHelper() {
    attachShadowHelper(buildMatchersFromResources(this))
}

/**
 * Attaches the platform helper with matchers built from the provided XML
 * resource. Must be called before setContentView.
 */
fun Activity.attachShadowHelper(@XmlRes xmlResId: Int) {
    attachShadowHelper(buildMatchersFromXml(this, xmlResId))
}

/**
 * Attaches the platform helper with the given list of matchers. Must be called
 * before setContentView.
 */
fun Activity.attachShadowHelper(matchers: List<TagMatcher>) {
    layoutInflater.factory = ShadowHelperFactory(this, matchers)
}

internal class ShadowHelperFactory(
    context: Context,
    matchers: List<TagMatcher>
) : LayoutInflater.Factory {

    private val helper = ShadowHelper(context, matchers)

    override fun onCreateView(
        name: String,
        context: Context,
        attrs: AttributeSet
    ) = helper.processTag(name, context, attrs)
}

/**
 * A simple Application subclass that unconditionally sets the platform shadow
 * inflater helper on every Activity instance that is created. Included mainly
 * for illustrative purposes.
 */
class ShadowHelperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityCreatedCallback {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                activity.attachShadowHelper()
            }
        })
    }
}

/**
 * This is an adapter interface with empty defaults for all of
 * ActivityLifecycleCallbacks functions except the one.
 */
interface ActivityCreatedCallback : Application.ActivityLifecycleCallbacks {

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