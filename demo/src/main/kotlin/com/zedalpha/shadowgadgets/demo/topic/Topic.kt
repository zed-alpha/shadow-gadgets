package com.zedalpha.shadowgadgets.demo.topic

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.getSpans
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zedalpha.shadowgadgets.demo.internal.ContentCardView

internal class Topic<T : TopicFragment<*>>(
    val title: String,
    private val descriptionResId: Int,
    private val fragmentClass: Class<T>
) {
    fun createFragment(): T = fragmentClass.getConstructor().newInstance()

    fun description(context: Context): CharSequence =
        context.getText(descriptionResId).let { text ->
            if (text !is SpannedString) return@let text

            SpannableString(text).apply {
                getSpans<StyleSpan>()
                    .filter { it.style == Typeface.ITALIC }
                    .forEach { span ->
                        val start = getSpanStart(span)
                        val end = getSpanEnd(span)
                        val mono = TypefaceSpan("sans-serif-monospace")
                        setSpan(mono, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                        val color = ForegroundColorSpan(0xff224466.toInt())
                        setSpan(color, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)
                        removeSpan(span)
                    }
            }
        }
}

abstract class TopicFragment<T : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T
) : Fragment() {

    protected lateinit var ui: T

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        ContentCardView(requireContext())
            .apply { ui = inflate(inflater, this, true) }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        loadUi(ui)

    abstract fun loadUi(ui: T)
}