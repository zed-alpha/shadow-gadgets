package com.zedalpha.shadowgadgets.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = supportFragmentManager

        val showcaseFragment: ShowcaseFragment
        val inflationFragment: InflationFragment

        if (savedInstanceState == null) {
            showcaseFragment = ShowcaseFragment()
            inflationFragment = InflationFragment()
            manager.beginTransaction()
                .add(R.id.container, showcaseFragment, TAG_SHOWCASE)
                .add(R.id.container, inflationFragment, TAG_INFLATION)
                .hide(inflationFragment)
                .commit()
        } else {
            showcaseFragment =
                manager.findFragmentByTag(TAG_SHOWCASE) as ShowcaseFragment
            inflationFragment =
                manager.findFragmentByTag(TAG_INFLATION) as InflationFragment
        }

        findViewById<BottomNavigationView>(R.id.nav_view).setOnItemSelectedListener {
            val showFirst = it.itemId == R.id.item_showcase
            manager.beginTransaction()
                .setCustomAnimations(
                    if (showFirst) R.anim.slide_in_left else R.anim.slide_in_right,
                    if (showFirst) R.anim.slide_out_right else R.anim.slide_out_left
                )
                .show(if (showFirst) showcaseFragment else inflationFragment)
                .hide(if (showFirst) inflationFragment else showcaseFragment)
                .commit()
            true
        }
    }
}

private const val TAG_SHOWCASE = "showcase"
private const val TAG_INFLATION = "inflation"