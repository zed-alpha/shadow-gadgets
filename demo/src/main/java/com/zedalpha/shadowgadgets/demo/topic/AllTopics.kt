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
    isToggleable: Boolean,
    minSdk: Int = Build.VERSION_CODES.LOLLIPOP
) {
    private val isAvailable = Build.VERSION.SDK_INT >= minSdk

    val shouldShowToggle = isAvailable && isToggleable

    fun createFragment(): Fragment =
        if (isAvailable) fragmentClass.newInstance() else UnavailableFragment()

    override fun toString() = title
}

@SuppressLint("NewApi")
internal val Topics = listOf(
    Topic(Overlays1Fragment::class.java, "Overlays 1", true),
    Topic(Overlays2Fragment::class.java, "Overlays 2", true),
    Topic(ContainersFragment::class.java, "Containers", false),
    Topic(DrawablesFragment::class.java, "Drawables", false),
    Topic(ColorsFragment::class.java, "Colors", true, Build.VERSION_CODES.P),
    Topic(InflationFragment::class.java, "Inflation", false),
    Topic(LimitationsFragment::class.java, "Limitations", true)
)

sealed class TopicFragment(layoutResId: Int) : Fragment(layoutResId) {
    abstract val targetIds: IntArray

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