package com.zedalpha.shadowgadgets.inflation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.ShadowXmlAttributes
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.shadowXmlAttributes
import java.lang.reflect.Array


internal class ShadowHelper(
    private val context: Context,
    private val matchers: List<TagMatcher>
) {
    private val inflater by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ViewInflater(context)
        } else {
            OldViewInflater(context)
        }
    }

    fun processTag(name: String, attrs: AttributeSet): View? {
        val view = if (name !in IGNORED_TAGS) inflater.tryCreate(name, attrs) else null
        if (view != null) {
            val attributes = extractShadowAttributes(context, attrs)
            if (attributes.clip || checkMatchers(view, name, attrs)) {
                view.shadowXmlAttributes = attributes
                view.clipOutlineShadow = true
            }
        }
        return view
    }

    fun checkMatchers(view: View, tagName: String, attrs: AttributeSet): Boolean {
        for (matcher in matchers) {
            if (matcher.matches(view, tagName, attrs)) return true
        }
        return false
    }

    private fun extractShadowAttributes(
        context: Context,
        attrs: AttributeSet
    ): ShadowXmlAttributes {
        val array = context.obtainStyledAttributes(attrs, R.styleable.OverlayShadow)
        val clip = array.getBoolean(R.styleable.OverlayShadow_clipOutlineShadow, false)
        val animate = array.getBoolean(R.styleable.OverlayShadow_disableShadowAnimation, false)
        array.recycle()
        return ShadowXmlAttributes(clip, animate)
    }
}

private open class ViewInflater(context: Context) : LayoutInflater(context) {
    // Duplicate parts of [Phone]LayoutInflater 'cause we're jumping into the middle.
    open fun tryCreate(name: String, attrs: AttributeSet): View? {
        return try {
            if (name.indexOf('.') == -1) {
                onCreateView(null, name, attrs)
            } else {
                createView(name, null, attrs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateView(name: String, attrs: AttributeSet): View {
        for (prefix in CLASS_PREFIXES) {
            try {
                createView(name, prefix, attrs)?.let { return it }
            } catch (e: Exception) {
                e.printStackTrace()
                /* noop */
            }
        }
        return super.onCreateView(name, attrs)
    }

    override fun cloneInContext(newContext: Context) = throw UnsupportedOperationException()
}

@SuppressLint("SoonBlockedPrivateApi")
private class OldViewInflater(context: Context) : ViewInflater(context) {
    private val mConstructorArgs = try {
        LayoutInflater::class.java.getDeclaredField("mConstructorArgs")
            .apply { isAccessible = true }.get(this)
    } catch (e: Exception) {
        null
    }

    override fun tryCreate(name: String, attrs: AttributeSet): View? {
        val args = mConstructorArgs
        val reset = args != null && Array.get(args, 0) == null
        if (reset) Array.set(args!!, 0, context)
        val view = super.tryCreate(name, attrs)
        if (reset) Array.set(args!!, 0, null)
        return view
    }
}

private val IGNORED_TAGS = arrayOf("include", "merge", "requestFocus", "tag", "fragment", "blink")

private val CLASS_PREFIXES = arrayOf("android.widget.", "android.webkit.", "android.app.")