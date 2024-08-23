package com.zedalpha.shadowgadgets.view.inflation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatViewInflater
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.core.app.ComponentActivity
import com.google.android.material.theme.MaterialComponentsViewInflater
import com.zedalpha.shadowgadgets.view.R

/**
 * Attaches the layout inflation helper for Material Components themes.
 *
 * Must be called before `super.onCreate()`.
 */
fun ComponentActivity.attachMaterialComponentsShadowHelper() {
    theme.applyStyle(
        R.style.ThemeOverlay_ShadowGadgets_MaterialComponents,
        true
    )
}

/**
 * A specialized [AppCompatViewInflater] that hooks into the layout inflation
 * pipeline to process the library's custom attributes.
 *
 * This is not meant to be used directly, but it must be public so that the
 * androidx appcompat framework has access. It can be applied either through
 * resources with an XML theme attribute, or by doing the equivalent thing in
 * code with a helper function.
 *
 * For example, using an XML theme:
 *
 * ```xml
 * <style name="Theme.YourApp" parent="Theme.MaterialComponents…">
 *     …
 *     <!-- Or use the fully qualified class name in place of the @string. -->
 *     <item name="viewInflaterClass">
 *         @string/material_components_shadow_helper
 *     </item>
 * </style>
 * ```
 *
 * Or, using a helper function, which simply adds the attribute setting to the
 * current theme programmatically:
 *
 * ```kotlin
 * class YourActivity : AppCompatActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         attachMaterialComponentsShadowHelper()
 *         super.onCreate(savedInstanceState)
 *         …
 *     }
 * }
 * ```
 */
@Suppress("unused")
class MaterialComponentsShadowHelper : MaterialComponentsViewInflater() {

    private var helper: InflationHelper? = null

    private fun ensureHelper(context: Context): InflationHelper =
        helper ?: InflationHelper(context).also { helper = it }

    private fun <T : View> checkView(
        view: T,
        tagName: String,
        attrs: AttributeSet
    ): T {
        ensureHelper(view.context).processView(view, tagName, attrs)
        return view
    }

    override fun createView(
        context: Context,
        name: String,
        attrs: AttributeSet
    ): View? = ensureHelper(context).processTag(name, context, attrs)

    override fun createTextView(
        context: Context,
        attrs: AttributeSet
    ): AppCompatTextView {
        return checkView(
            super.createTextView(context, attrs),
            "TextView",
            attrs
        )
    }

    override fun createImageView(
        context: Context,
        attrs: AttributeSet
    ): AppCompatImageView {
        return checkView(
            super.createImageView(context, attrs),
            "ImageView",
            attrs
        )
    }

    override fun createButton(
        context: Context,
        attrs: AttributeSet
    ): AppCompatButton {
        return checkView(super.createButton(context, attrs), "Button", attrs)
    }

    override fun createEditText(
        context: Context,
        attrs: AttributeSet
    ): AppCompatEditText {
        return checkView(
            super.createEditText(context, attrs),
            "EditText",
            attrs
        )
    }

    override fun createSpinner(
        context: Context,
        attrs: AttributeSet
    ): AppCompatSpinner {
        return checkView(super.createSpinner(context, attrs), "Spinner", attrs)
    }

    override fun createImageButton(
        context: Context,
        attrs: AttributeSet
    ): AppCompatImageButton {
        return checkView(
            super.createImageButton(context, attrs),
            "ImageButton",
            attrs
        )
    }

    override fun createCheckBox(
        context: Context,
        attrs: AttributeSet
    ): AppCompatCheckBox {
        return checkView(
            super.createCheckBox(context, attrs),
            "CheckBox",
            attrs
        )
    }

    override fun createRadioButton(
        context: Context,
        attrs: AttributeSet
    ): AppCompatRadioButton {
        return checkView(
            super.createRadioButton(context, attrs),
            "RadioButton",
            attrs
        )
    }

    override fun createCheckedTextView(
        context: Context,
        attrs: AttributeSet
    ): AppCompatCheckedTextView {
        return checkView(
            super.createCheckedTextView(context, attrs),
            "CheckedTextView",
            attrs
        )
    }

    override fun createAutoCompleteTextView(
        context: Context,
        attrs: AttributeSet?
    ): AppCompatAutoCompleteTextView {
        return checkView(
            super.createAutoCompleteTextView(context, attrs),
            "AutoCompleteTextView",
            attrs!!
        )
    }

    override fun createMultiAutoCompleteTextView(
        context: Context,
        attrs: AttributeSet
    ): AppCompatMultiAutoCompleteTextView {
        return checkView(
            super.createMultiAutoCompleteTextView(context, attrs),
            "MultiAutoCompleteTextView",
            attrs
        )
    }

    override fun createRatingBar(
        context: Context,
        attrs: AttributeSet
    ): AppCompatRatingBar {
        return checkView(
            super.createRatingBar(context, attrs),
            "RatingBar",
            attrs
        )
    }

    override fun createSeekBar(
        context: Context,
        attrs: AttributeSet
    ): AppCompatSeekBar {
        return checkView(super.createSeekBar(context, attrs), "SeekBar", attrs)
    }

    override fun createToggleButton(
        context: Context,
        attrs: AttributeSet
    ): AppCompatToggleButton {
        return checkView(
            super.createToggleButton(context, attrs),
            "ToggleButton",
            attrs
        )
    }
}