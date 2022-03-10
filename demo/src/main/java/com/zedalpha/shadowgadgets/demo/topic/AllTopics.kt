package com.zedalpha.shadowgadgets.demo.topic

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.zedalpha.shadowgadgets.clipOutlineShadow
import com.zedalpha.shadowgadgets.demo.MainActivity
import com.zedalpha.shadowgadgets.demo.R


internal class Topic(
    private val fragmentClass: Class<out TopicFragment>,
    val title: String,
    minSdk: Int = Build.VERSION_CODES.LOLLIPOP
) {
    private val isAvailable = Build.VERSION.SDK_INT >= minSdk

    fun createFragment(): TopicFragment =
        if (isAvailable) fragmentClass.newInstance() else UnavailableFragment()

    override fun toString() = title
}

@SuppressLint("NewApi")
internal val Topics = listOf(
    Topic(Overlays1Fragment::class.java, "Overlays 1"),
    Topic(Overlays2Fragment::class.java, "Overlays 2"),
    Topic(ContainersFragment::class.java, "Containers"),
    Topic(DrawablesFragment::class.java, "Drawables"),
    Topic(ColorsFragment::class.java, "Colors", Build.VERSION_CODES.P),
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

class UnavailableFragment : TopicFragment(R.layout.fragment_unavailable) {
    override val targetIds = intArrayOf()
}