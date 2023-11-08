package com.zedalpha.shadowgadgets.view.inflation

import android.app.Activity
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import com.zedalpha.shadowgadgets.view.R


/**
 * The interface used by the inflation helpers to select Views using custom
 * criteria.
 *
 * On their own, the helpers only look for tags with the library's
 * `app:clipOutlineShadow` attribute. For those cases where modifying the
 * relevant layouts is impossible or infeasible, any number of TagMatchers can
 * be provided to the helpers in order to select Views by any other criteria
 * necessary. Like the inflation helpers, these matchers can be created and
 * applied in either XML resources, or in code.
 *
 * **Creating matchers in resources:**
 *
 * Matchers created in XML can only compare against the layout tag names and
 * `android:id` values. They are defined in res/xml/ files with a very
 * simplistic language: the `<id>` and `<name>` tags, and the `android:id`,
 * `android:name`, and `app:matchRule` attributes (note the `app` prefix). The
 * parser is extremely lenient and simply ignores everything else.
 *
 * The following snippet demonstrates all of the recognized element
 * compositions:
 *
 * ```xml
 * <matchers
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto">
 *
 *     <id android:id="@id/target_button" />
 *     <id android:name="target_button" />
 *     <id android:name="target_" app:matchRule="startsWith" />
 *     <name android:name="ImageButton" />
 *     <name android:name="Button" app:matchRule="endsWith" />
 * </matchers>
 * ```
 *
 * These all will match an `<ImageButton>` with
 * `android:id="@+id/target_button"`.
 *
 * An `<id>` element creates a TagMatcher that matches either the exact layout
 * ID int using the `android:id` attribute, or the ID name using `android:name`
 * and an `app:matchRule` (see [MatchRule]), which is `equals` by default.
 *
 * The `<name>` element creates a TagMatcher that matches against the layout
 * XML tag names, again using the `android:name` and `app:matchRule` attributes.
 *
 *
 * The res/xml/ files can be supplied to the inflation helper in a few different
 * ways.
 *
 * When using a zero-argument `attach*ShadowHelper` overload, or when the
 * inflation helper itself is set through resources, it looks for an XML file
 * reference first in the
 * [`shadowTagMatchers`][com.zedalpha.shadowgadgets.view.R.attr.shadowTagMatchers]
 * theme attribute. For example:
 *
 * ```xml
 * <style name="Theme.YourApp" parent="Theme.…">
 *     …
 *     <item name="shadowTagMatchers">@xml/matchers</item>
 * </style>
 * ```
 *
 * If there isn't one there, it then looks for a `<meta-data>` element in the
 * manifest, first in the `<activity>` element, then in the `<application>`.
 *
 * ```xml
 * <application …>
 *     <activity
 *         android:name=".YourActivity"
 *         android:theme="@style/Theme.YourApp">
 *         <meta-data
 *             android:name="com.zedalpha.shadowgadgets.SHADOW_TAG_MATCHERS"
 *             android:resource="@xml/matchers" />
 *     </activity>
 *     <!-- Also valid in the application. -->
 * </application>
 * ```
 *
 * Alternatively, an XML file can be passed directly to the helper in code,
 * using the appropriate attach function overload:
 *
 * ```kotlin
 * class YourActivity : Activity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         // All of the attach*ShadowHelper functions have this overload.
 *         attachShadowHelper(R.xml.matchers)
 *         super.onCreate(savedInstanceState)
 *         …
 *     }
 * }
 * ```
 *
 * **Creating matchers in code:**
 *
 * TagMatcher is simply an interface, so it can easily be implemented to perform
 * whatever other checks are necessary to select target Views from the
 * available information. The fully constructed View, its tag name, and the
 * AttributeSet pulled from the XML are all passed in to the match function.
 *
 * The two predefined matchers that are available in XML are accessible here
 * through the [idMatcher] and [nameMatcher] functions. For example, this
 * snippet creates the exact same matcher list as the XML example above:
 *
 * ```kotlin
 * class YourActivity : Activity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         // All of the attach*ShadowHelper functions have this overload.
 *         attachShadowHelper(
 *             listOf(
 *                 idMatcher(R.id.target_button),
 *                 idMatcher(matchName = "target_button"),
 *                 idMatcher(
 *                     matchName = "target_",
 *                     matchRule = MatchRule.StartsWith
 *                 ),
 *                 nameMatcher("ImageButton"),
 *                 nameMatcher("Button", MatchRule.EndsWith)
 *             )
 *         )
 *         super.onCreate(savedInstanceState)
 *         …
 *     }
 * }
 * ```
 */
interface TagMatcher {

    /**
     * Called after the [view] has been inflated from a layout. The [tagName] is
     * the name from the XML element, and [attrs] is the AttributeSet pulled
     * from it.
     *
     * Return true to have the inflater helper call
     * [clipOutlineShadow][com.zedalpha.shadowgadgets.view.clipOutlineShadow]=true
     * for the given View.
     *
     * The View is fully constructed by this point, so any other desired
     * settings can be made here, as well; e.g., the newer
     * [outlineShadowColorCompat][com.zedalpha.shadowgadgets.view.outlineShadowColorCompat],
     * which was not yet available when the inflation tools were created.
     */
    fun matches(view: View, tagName: String, attrs: AttributeSet): Boolean
}

/**
 * The comparison rules used for string values in tag matching.
 */
enum class MatchRule(private val method: String.(String) -> Boolean) {

    /** Compares using String::equals. */
    Equals(String::equals),

    /** Compares using String::contains. */
    Contains(String::contains),

    /** Compares using String::startsWith. */
    StartsWith(String::startsWith),

    /** Compares using String::endsWith. */
    EndsWith(String::endsWith);

    internal fun match(s1: String, s2: String) = s1.method(s2)

    internal companion object {
        // Maps XML attribute values to enum values.
        fun forValue(value: Int) = when (value) {
            1 -> Contains
            2 -> StartsWith
            3 -> EndsWith
            else -> Equals
        }
    }
}

/**
 * Creates a matcher to match a specific R.id exactly, or to match (possibly
 * multiple) ID names by a given comparison rule.
 */
fun idMatcher(
    @IdRes matchId: Int = View.NO_ID,
    matchName: String? = null,
    matchRule: MatchRule = MatchRule.Equals
): TagMatcher = IdMatcher(matchId, matchName, matchRule)

/**
 * Creates a matcher to match (possibly multiple) tag names by a given
 * comparison rule.
 */
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