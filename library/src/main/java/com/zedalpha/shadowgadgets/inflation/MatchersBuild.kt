package com.zedalpha.shadowgadgets.inflation

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.util.TypedValue
import android.util.Xml
import android.view.View
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.unwrapActivity
import org.xmlpull.v1.XmlPullParser


const val META_DATA_TAG_MATCHERS = "com.zedalpha.shadowgadgets.SHADOW_TAG_MATCHERS"

internal fun buildMatchersFromXml(context: Context, xmlResId: Int): List<TagMatcher> {
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
        parser.close()
    } catch (e: Exception) {
        /* ignore */
    }
    return matchers
}

internal fun buildMatchersFromResources(context: Context): List<TagMatcher> {
    val xmlResId =
        context.themeAttributeValue ?: unwrapActivity(context)?.metaDataValue
        ?: context.appMetaDataValue ?: 0

    return if (xmlResId == 0) emptyList() else buildMatchersFromXml(context, xmlResId)
}

private val Context.themeAttributeValue
    get() = TypedValue().let {
        if (theme.resolveAttribute(R.attr.shadowTagMatchers, it, true)) it.resourceId else null
    }

private val Activity.metaDataValue
    get() = packageManager.getActivityInfo(
        componentName,
        PackageManager.GET_META_DATA
    ).metaData?.getInt(META_DATA_TAG_MATCHERS)

private val Context.appMetaDataValue
    get() = packageManager.getApplicationInfo(
        packageName,
        PackageManager.GET_META_DATA
    ).metaData?.getInt(META_DATA_TAG_MATCHERS)

private fun processIdMatcher(
    parser: XmlResourceParser,
    context: Context,
    matchers: MutableList<TagMatcher>
) {
    val array = context.obtainStyledAttributes(Xml.asAttributeSet(parser), R.styleable.IdMatcher)
    val id = array.getResourceId(R.styleable.IdMatcher_android_id, View.NO_ID)
    val name = array.getString(R.styleable.IdMatcher_android_name)
    val rule = array.getInt(R.styleable.IdMatcher_matchRule, 0)
    array.recycle()

    if (id != View.NO_ID || name != null) matchers += IdMatcher(id, name, ruleForValue(rule))
}

private fun processNameMatcher(
    parser: XmlResourceParser,
    context: Context,
    matchers: MutableList<TagMatcher>
) {
    val array = context.obtainStyledAttributes(Xml.asAttributeSet(parser), R.styleable.NameMatcher)
    val name = array.getString(R.styleable.NameMatcher_android_name)
    val rule = array.getInt(R.styleable.NameMatcher_matchRule, 0)
    array.recycle()

    if (name != null) matchers += NameMatcher(name, ruleForValue(rule))
}

private fun ruleForValue(value: Int) = MatchRule.values().getOrElse(value) { MatchRule.Equals }