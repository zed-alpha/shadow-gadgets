package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import com.zedalpha.shadowgadgets.view.R


interface TagMatcher {
    fun matches(view: View, tagName: String, attrs: AttributeSet): Boolean
}

enum class MatchRule(private val method: String.(String) -> Boolean) {

    Equals(String::equals),
    Contains(String::contains),
    StartsWith(String::startsWith),
    EndsWith(String::endsWith);

    internal fun match(s1: String, s2: String) = s1.method(s2)

    internal companion object {
        fun forValue(value: Int) = when (value) {
            1 -> Contains
            2 -> StartsWith
            3 -> EndsWith
            else -> Equals
        }
    }
}

fun idMatcher(
    @IdRes matchId: Int = View.NO_ID,
    matchName: String? = null,
    matchRule: MatchRule = MatchRule.Equals
): TagMatcher = IdMatcher(matchId, matchName, matchRule)

fun nameMatcher(
    matchName: String,
    matchRule: MatchRule = MatchRule.Equals
): TagMatcher = NameMatcher(matchName, matchRule)

internal class IdMatcher(
    @IdRes private val matchId: Int = View.NO_ID,
    private val matchName: String? = null,
    private val matchRule: MatchRule = MatchRule.Equals
) : TagMatcher {

    override fun matches(
        view: View,
        tagName: String,
        attrs: AttributeSet
    ): Boolean {
        val id = view.id
        return when {
            id == View.NO_ID -> false
            matchId != View.NO_ID && matchId == id -> true
            matchName != null && matchRule.match(view.idName, matchName) -> true
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

private val View.idName: String
    get() = resources.getResourceEntryName(id)

internal var Activity.tagMatchers: List<TagMatcher>?
    @Suppress("UNCHECKED_CAST")
    get() = window.decorView.getTag(R.id.tag_matchers) as? List<TagMatcher>
    set(value) = window.decorView.setTag(R.id.tag_matchers, value)