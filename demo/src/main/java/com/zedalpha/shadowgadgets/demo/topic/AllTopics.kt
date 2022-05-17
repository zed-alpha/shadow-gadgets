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
    val showFallbackWarning: Boolean = false,
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
    Topic(IntroFragment::class.java, "Intro"),
    Topic(MotionsFragment::class.java, "Motions"),
    Topic(ViewGroupsFragment::class.java, "ViewGroups"),
    Topic(LimitationsFragment::class.java, "Limitations"),
    Topic(IssuesFragment::class.java, "Issues", showFallbackWarning = true),
    Topic(OptionsFragment::class.java, "Options", showFallbackWarning = true),
    Topic(InflationFragment::class.java, "Inflation"),
    Topic(
        ColorsFragment::class.java,
        "Colors",
        isAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
        unavailableMessage = R.string.unavailable_colors
    ),
    Topic(
        DrawableFragment::class.java,
        "Drawable",
        isAvailable = ShadowDrawable.isAvailable,
        unavailableMessage = R.string.unavailable_drawables
    )
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

class UnavailableFragment : TopicFragment(R.layout.fragment_unavailable) {
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