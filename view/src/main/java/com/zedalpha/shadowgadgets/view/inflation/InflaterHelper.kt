package com.zedalpha.shadowgadgets.view.inflation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.*
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.forceShadowColorCompat
import com.zedalpha.shadowgadgets.view.internal.extractShadowAttributes
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import java.lang.reflect.Array


internal class ShadowHelper(
    private val context: Context,
    private val matchers: List<TagMatcher>
) {
    private val inflater: ViewInflater by lazy {
        if (Build.VERSION.SDK_INT >= 29) {
            NewViewInflater(context)
        } else {
            OldViewInflater(context)
        }
    }

    fun processTag(name: String, context: Context, attrs: AttributeSet): View? {
        val view = if (name !in IgnoredTags) {
            inflater.tryCreate(name, context, attrs)
        } else {
            null
        }
        if (view != null) {
            val attributes = attrs.extractShadowAttributes(context)
            if (attributes.clipOutlineShadow ||
                checkMatchers(view, name, attrs)
            ) {
                view.outlineShadowColorCompat =
                    attributes.outlineShadowColorCompat
                view.forceShadowColorCompat = attributes.forceShadowColorCompat
                view.clippedShadowPlane = attributes.clippedShadowPlane
                view.clipOutlineShadow = true
            }
        }
        return view
    }

    fun checkMatchers(
        view: View,
        tagName: String,
        attrs: AttributeSet
    ): Boolean {
        for (matcher in matchers) {
            if (matcher.matches(view, tagName, attrs)) return true
        }
        return false
    }
}

@SuppressLint("SoonBlockedPrivateApi")
private class OldViewInflater(context: Context) : ViewInflater(context) {

    @SuppressLint("PrivateApi")
    private val mConstructorArgs = try {
        LayoutInflater::class.java.getDeclaredField("mConstructorArgs")
            .apply { isAccessible = true }.get(this)
    } catch (e: Exception) {
        null
    }

    override fun tryCreate(
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        val args = mConstructorArgs
        val setViewContext = args != null && Array.get(args, 0) == null
        if (setViewContext) Array.set(args!!, 0, context)
        val view = try {
            if (name.indexOf('.') == -1) {
                onCreateView(null, name, attrs)
            } else {
                createView(name, null, attrs)
            }
        } catch (e: Exception) {
            null
        }
        if (setViewContext) Array.set(args!!, 0, null)
        return view
    }
}

@RequiresApi(29)
private class NewViewInflater(context: Context) : ViewInflater(context) {

    override fun tryCreate(
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        return try {
            if (name.indexOf('.') == -1) {
                onCreateView(context, null, name, attrs)
            } else {
                createView(context, name, null, attrs)
            }
        } catch (e: Exception) {
            null
        }
    }
}

private sealed class ViewInflater(context: Context) : LayoutInflater(context) {

    abstract fun tryCreate(
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View?

    final override fun onCreateView(name: String, attrs: AttributeSet): View {
        for (prefix in ClassPrefixes) {
            try {
                createView(name, prefix, attrs)?.let { return it }
            } catch (e: Exception) {
                // ignore
            }
        }
        return super.onCreateView(name, attrs)
    }

    final override fun cloneInContext(newContext: Context) =
        throw UnsupportedOperationException()
}

private val IgnoredTags =
    arrayOf("include", "merge", "requestFocus", "tag", "fragment", "blink")

private val ClassPrefixes =
    arrayOf("android.widget.", "android.webkit.", "android.app.")