package com.zedalpha.shadowgadgets.view.internal

import android.content.res.Resources
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.CallSuper
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

internal val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)

internal inline val View.parentViewGroup: ViewGroup?
    get() = this.parent as? ViewGroup

internal val ViewGroup.isRecycling: Boolean
    get() = this is RecyclerView || this is AdapterView<*>

internal fun View.fastLayout(left: Int, top: Int, right: Int, bottom: Int) {
    if (Build.VERSION.SDK_INT >= 29) {
        ViewPositionHelper.setPosition(this, left, top, right, bottom)
    } else {
        this.layout(left, top, right, bottom)
    }
}

@RequiresApi(29)
private object ViewPositionHelper {

    @DoNotInline
    fun setPosition(view: View, left: Int, top: Int, right: Int, bottom: Int) =
        view.setLeftTopRightBottom(left, top, right, bottom)
}

internal fun interface OnLayoutChangeSizeAdapter : View.OnLayoutChangeListener {

    fun onSizeChange(width: Int, height: Int)

    @CallSuper
    override fun onLayoutChange(
        view: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) =
        onSizeChange(right - left, bottom - top)
}

internal fun View.debugId(): String =
    buildString {
        append(this@debugId.javaClass.simpleName)
        if (id != View.NO_ID) {
            try {
                append(", R.id.${resources.getResourceEntryName(id)}")
                return@buildString
            } catch (_: Resources.NotFoundException) {
                // fall through
            }
        }
        append(", id=$id")
    }