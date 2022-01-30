package com.zedalpha.shadowgadgets.inflation

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.zedalpha.shadowgadgets.R


interface TagMatcher {
    fun matches(view: View, tagName: String, attrs: AttributeSet): Boolean
}

enum class MatchRule(private val method: String.(String) -> Boolean) {
    Equals(String::equals),
    Contains(String::contains),
    StartsWith(String::startsWith),
    EndsWith(String::endsWith);

    internal fun match(s1: String, s2: String) = s1.method(s2)
}

fun idMatcher(
    matchId: Int = View.NO_ID,
    matchName: String? = null,
    matchRule: MatchRule = MatchRule.Equals
): TagMatcher = IdMatcher(matchId, matchName, matchRule)

fun nameMatcher(
    matchName: String,
    matchRule: MatchRule = MatchRule.Equals
): TagMatcher = NameMatcher(matchName, matchRule)

internal class IdMatcher(
    private val matchId: Int = View.NO_ID,
    private val matchName: String? = null,
    private val matchRule: MatchRule = MatchRule.Equals
) : TagMatcher {
    override fun matches(view: View, tagName: String, attrs: AttributeSet): Boolean {
        val context = view.context
        val array = context.obtainStyledAttributes(attrs, R.styleable.IdMatcher)
        val id = array.getResourceId(R.styleable.IdMatcher_android_id, View.NO_ID)
        array.recycle()

        return when {
            id == View.NO_ID -> false
            matchId != View.NO_ID && matchId == id -> true
            matchName != null && matchRule.match(context.resourceEntryName(id), matchName) -> true
            else -> false
        }
    }
}

internal class NameMatcher(
    private val matchName: String,
    private val matchRule: MatchRule = MatchRule.Equals
) : TagMatcher {
    override fun matches(view: View, tagName: String, attrs: AttributeSet) =
        matchRule.match(tagName, matchName)
}

private fun Context.resourceEntryName(id: Int): String = resources.getResourceEntryName(id)

internal var Activity.matchers: List<TagMatcher>?
    @Suppress("UNCHECKED_CAST")
    get() = window.decorView.getTag(R.id.tag_decor_shadow_matchers) as? List<TagMatcher>
    set(value) {
        window.decorView.setTag(R.id.tag_decor_shadow_matchers, value)
    }