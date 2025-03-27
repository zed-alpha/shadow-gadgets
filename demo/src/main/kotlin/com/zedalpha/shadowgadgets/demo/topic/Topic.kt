package com.zedalpha.shadowgadgets.demo.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.zedalpha.shadowgadgets.demo.internal.ContentCardView

internal class Topic<T : TopicFragment<*>>(
    val title: String,
    val descriptionResId: Int,
    private val fragmentClass: Class<T>
) {
    fun createFragment(): T = fragmentClass.getConstructor().newInstance()
}

abstract class TopicFragment<T : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T
) : Fragment() {

    protected lateinit var ui: T

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ContentCardView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        ui = inflate(inflater, this, true)
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadUi(ui)
    }

    abstract fun loadUi(ui: T)
}