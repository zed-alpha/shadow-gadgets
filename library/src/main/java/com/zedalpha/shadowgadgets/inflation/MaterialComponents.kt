@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.inflation

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import com.google.android.material.theme.MaterialComponentsViewInflater
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.unwrapActivity


fun AppCompatActivity.attachMaterialComponentsShadowHelper() {
    attachMaterialComponentsShadowHelper(buildMatchersFromResources(this))
}

fun AppCompatActivity.attachMaterialComponentsShadowHelper(@XmlRes xmlResId: Int) {
    attachMaterialComponentsShadowHelper(buildMatchersFromXml(this, xmlResId))
}

fun AppCompatActivity.attachMaterialComponentsShadowHelper(matchers: List<TagMatcher>) {
    this.matchers = matchers
    theme.applyStyle(R.style.ThemeOverlay_ShadowGadgets_MaterialComponents, true)
}

// Must be public. Instantiated by AppCompatDelegateImpl from theme attribute value.
class MaterialComponentsShadowHelper : MaterialComponentsViewInflater() {
    private lateinit var helper: ShadowHelper

    private fun ensureHelper(context: Context) {
        if (!this::helper.isInitialized) {
            // Try build again if null, in case someone else is using this class.
            val matchers = unwrapActivity(context)?.matchers ?: buildMatchersFromResources(context)
            helper = ShadowHelper(context, matchers)
        }
    }

    private fun <T : View> checkView(view: T, tagName: String, attrs: AttributeSet): T {
        ensureHelper(view.context)
        if (helper.checkMatchers(view, tagName, attrs)) view.clipOutlineShadow = true
        return view
    }

    override fun createView(context: Context, name: String, attrs: AttributeSet): View? {
        ensureHelper(context)
        return helper.processTag(name, context, attrs)
    }

    // Sloppy nullability annotation in MaterialComponentsViewInflater is
    // causing inconsistent signatures in this subclass. Maybe file a bug.

    override fun createTextView(context: Context, attrs: AttributeSet): AppCompatTextView {
        return checkView(super.createTextView(context, attrs), "TextView", attrs)
    }

    override fun createImageView(context: Context, attrs: AttributeSet): AppCompatImageView {
        return checkView(super.createImageView(context, attrs), "ImageView", attrs)
    }

    override fun createButton(context: Context, attrs: AttributeSet): AppCompatButton {
        return checkView(super.createButton(context, attrs), "Button", attrs)
    }

    override fun createEditText(context: Context, attrs: AttributeSet): AppCompatEditText {
        return checkView(super.createEditText(context, attrs), "EditText", attrs)
    }

    override fun createSpinner(context: Context, attrs: AttributeSet): AppCompatSpinner {
        return checkView(super.createSpinner(context, attrs), "Spinner", attrs)
    }

    override fun createImageButton(context: Context, attrs: AttributeSet): AppCompatImageButton {
        return checkView(super.createImageButton(context, attrs), "ImageButton", attrs)
    }

    override fun createCheckBox(context: Context, attrs: AttributeSet): AppCompatCheckBox {
        return checkView(super.createCheckBox(context, attrs), "CheckBox", attrs)
    }

    override fun createRadioButton(context: Context, attrs: AttributeSet): AppCompatRadioButton {
        return checkView(super.createRadioButton(context, attrs), "RadioButton", attrs)
    }

    override fun createCheckedTextView(
        context: Context,
        attrs: AttributeSet
    ): AppCompatCheckedTextView {
        return checkView(super.createCheckedTextView(context, attrs), "CheckedTextView", attrs)
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

    override fun createRatingBar(context: Context, attrs: AttributeSet): AppCompatRatingBar {
        return checkView(super.createRatingBar(context, attrs), "RatingBar", attrs)
    }

    override fun createSeekBar(context: Context, attrs: AttributeSet): AppCompatSeekBar {
        return checkView(super.createSeekBar(context, attrs), "SeekBar", attrs)
    }

    override fun createToggleButton(
        context: Context,
        attrs: AttributeSet
    ): AppCompatToggleButton {
        return checkView(super.createToggleButton(context, attrs), "ToggleButton", attrs)
    }
}