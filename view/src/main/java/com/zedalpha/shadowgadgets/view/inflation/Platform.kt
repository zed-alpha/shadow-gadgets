package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.XmlRes


fun Activity.attachShadowHelper() {
    attachShadowHelper(buildMatchersFromResources(this))
}

fun Activity.attachShadowHelper(@XmlRes xmlResId: Int) {
    attachShadowHelper(buildMatchersFromXml(this, xmlResId))
}

fun Activity.attachShadowHelper(matchers: List<TagMatcher>) {
    layoutInflater.factory = ShadowHelperFactory(this, matchers)
}

internal class ShadowHelperFactory(
    context: Context,
    matchers: List<TagMatcher>
) :
    LayoutInflater.Factory {
    private val helper = ShadowHelper(context, matchers)

    override fun onCreateView(
        name: String,
        context: Context,
        attrs: AttributeSet
    ) = helper.processTag(name, context, attrs)
}

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

interface ActivityCreatedCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {}
}