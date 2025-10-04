package com.zedalpha.shadowgadgets.view.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class SwitchGroupTest {

    @Test
    fun modify() {
        val range = 1..10

        val group = SwitchGroup<Any>()
        assertEquals(group.size, 0)

        range.forEach { index ->
            group.add(index)
            assertEquals(group.size, index)
        }

        range.reversed().forEach { index ->
            assertEquals(group.size, index)
            group.remove(index)
        }

        assertEquals(group.size, 0)
    }
}