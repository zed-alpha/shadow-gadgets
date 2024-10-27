package com.zedalpha.shadowgadgets.view.inflation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.internal.extractShadowAttributes
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import java.lang.reflect.Array

internal class InflationHelper(private val context: Context) {

    private val inflater: ViewInflater =
        if (Build.VERSION.SDK_INT >= 29) {
            NewViewInflater(context)
        } else {
            OldViewInflater(context)
        }

    fun processTag(name: String, context: Context, attrs: AttributeSet): View? {
        val view = if (name in IgnoredTags) {
            null
        } else {
            inflater.tryCreate(name, context, attrs)
        }
        view ?: return null
        applyAttributes(view, attrs)
        return view
    }

    fun applyAttributes(view: View, attrs: AttributeSet) {
        val attributes = attrs.extractShadowAttributes(context)
        attributes.shadowPlane?.let { plane ->
            view.shadowPlane = plane
        }
        attributes.clipOutlineShadow?.let { clip ->
            view.clipOutlineShadow = clip
        }
        attributes.outlineShadowColorCompat?.let { color ->
            view.outlineShadowColorCompat = color
        }
        attributes.forceOutlineShadowColorCompat?.let { force ->
            view.forceOutlineShadowColorCompat = force
        }
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
    ): View? = try {
        if (name.indexOf('.') == -1) {
            onCreateView(context, null, name, attrs)
        } else {
            createView(context, name, null, attrs)
        }
    } catch (e: Exception) {
        null
    }
}

private abstract class ViewInflater(context: Context) :
    LayoutInflater(context) {

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