package com.zedalpha.shadowgadgets.demo.topic

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.demo.MainActivity
import com.zedalpha.shadowgadgets.demo.R
import com.zedalpha.shadowgadgets.drawable.ShadowDrawable


internal class Topic(
    private val fragmentClass: Class<out TopicFragment>,
    val title: String,
    private val isAvailable: Boolean = true,
    private val unavailableMessage: Int = 0
) {
    fun createFragment(): TopicFragment =
        if (isAvailable) fragmentClass.newInstance()
        else UnavailableFragment.withMessage(unavailableMessage)

    override fun toString() = title
}

@SuppressLint("NewApi")
internal val Topics = listOf(
    Topic(Overlays1Fragment::class.java, "Overlays 1"),
    Topic(Overlays2Fragment::class.java, "Overlays 2"),
    Topic(ViewGroupsFragment::class.java, "ViewGroups"),
    Topic(
        DrawablesFragment::class.java,
        "Drawables",
        ShadowDrawable.isAvailable,
        R.string.unavailable_drawables
    ),
    Topic(
        ColorsFragment::class.java,
        "Colors",
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
        R.string.unavailable_colors
    ),
    Topic(InflationFragment::class.java, "Inflation"),
    Topic(LimitationsFragment::class.java, "Limitations")
)

sealed class TopicFragment(layoutResId: Int) : Fragment(layoutResId) {
    protected abstract val targetIds: IntArray

    val shouldShowToggle: Boolean
        get() = targetIds.isNotEmpty()

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setTargetClippingEnabled((activity as MainActivity).isClippingEnabled)
    }

    fun setTargetClippingEnabled(enabled: Boolean) {
        targetIds.forEach { requireView().findViewById<View>(it).clipOutlineShadow = enabled }
    }
}

class UnavailableFragment private constructor() : TopicFragment(R.layout.fragment_unavailable) {
    override val targetIds = intArrayOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.text).setText(requireArguments().getInt(ARGUMENT_MESSAGE))
    }

    companion object {
        const val ARGUMENT_MESSAGE = "message"

        fun withMessage(msgResId: Int) = UnavailableFragment().apply {
            arguments = Bundle().also {
                it.putInt(ARGUMENT_MESSAGE, msgResId)
            }
        }
    }
}