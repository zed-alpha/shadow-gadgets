package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.util.TypedValue
import android.util.Xml
import android.view.View
import androidx.annotation.XmlRes
import com.zedalpha.shadowgadgets.view.R
import org.xmlpull.v1.XmlPullParser


const val META_DATA_TAG_MATCHERS =
    "com.zedalpha.shadowgadgets.SHADOW_TAG_MATCHERS"

internal fun buildMatchersFromXml(
    context: Context,
    @XmlRes xmlResId: Int
): List<TagMatcher> {
    val matchers = mutableListOf<TagMatcher>()
    val parser = context.resources.getXml(xmlResId)
    try {
        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "id" -> processIdMatcher(parser, context, matchers)
                    "name" -> processNameMatcher(parser, context, matchers)
                }
            }
            event = parser.next()
        }
    } catch (e: Exception) {
        // ignore
    } finally {
        parser.close()
    }
    return matchers
}

internal fun buildMatchersFromResources(context: Context): List<TagMatcher> {
    val xmlResId = getThemeAttributeValue(context)
        ?: getActivityMetaDataValue(context)
        ?: getApplicationMetaDataValue(context)
        ?: 0

    return if (xmlResId != 0) {
        buildMatchersFromXml(context, xmlResId)
    } else {
        emptyList()
    }
}

private fun getThemeAttributeValue(context: Context): Int? {
    val value = TypedValue()
    val success =
        context.theme.resolveAttribute(R.attr.shadowTagMatchers, value, true)
    return if (success) value.resourceId else null
}

private fun getActivityMetaDataValue(context: Context): Int? {
    val activity = unwrapActivity(context)
    return if (activity != null) {
        activity.packageManager.getActivityInfo(
            activity.componentName,
            PackageManager.GET_META_DATA
        ).metaData?.getInt(META_DATA_TAG_MATCHERS)
    } else {
        null
    }
}

private fun getApplicationMetaDataValue(context: Context): Int? {
    return context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData?.getInt(META_DATA_TAG_MATCHERS)
}

private fun processIdMatcher(
    parser: XmlResourceParser,
    context: Context,
    matchers: MutableList<TagMatcher>
) {
    val array = context.obtainStyledAttributes(
        Xml.asAttributeSet(parser),
        R.styleable.IdMatcher
    )
    val id = array.getResourceId(R.styleable.IdMatcher_android_id, View.NO_ID)
    val name = array.getString(R.styleable.IdMatcher_android_name)
    val rule = array.getInt(R.styleable.IdMatcher_matchRule, 0)
    array.recycle()

    if (id != View.NO_ID || name != null) {
        matchers += IdMatcher(id, name, MatchRule.forValue(rule))
    }
}

private fun processNameMatcher(
    parser: XmlResourceParser,
    context: Context,
    matchers: MutableList<TagMatcher>
) {
    val array = context.obtainStyledAttributes(
        Xml.asAttributeSet(parser),
        R.styleable.NameMatcher
    )
    val name = array.getString(R.styleable.NameMatcher_android_name)
    val rule = array.getInt(R.styleable.NameMatcher_matchRule, 0)
    array.recycle()

    if (name != null) matchers += NameMatcher(name, MatchRule.forValue(rule))
}

internal fun unwrapActivity(context: Context): Activity? {
    var checkContext: Context? = context
    do {
        if (checkContext is Activity) return checkContext
        checkContext = (checkContext as? ContextWrapper)?.baseContext
    } while (checkContext != null)
    return null
}