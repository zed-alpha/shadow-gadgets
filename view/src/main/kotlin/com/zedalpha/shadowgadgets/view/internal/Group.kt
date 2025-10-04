package com.zedalpha.shadowgadgets.view.internal

import androidx.annotation.CallSuper

internal interface Group<E> {
    val size: Int
    val isEmpty: Boolean
    fun add(member: E)
    fun remove(member: E)
    fun iterate(block: (E) -> Unit)
    fun leading(condition: (E) -> Boolean): E?
    fun has(condition: (E) -> Boolean): Boolean
}

internal open class SwitchGroup<E> : Group<E> {

    private var members: SwitchList<E> = SwitchList.Empty

    final override val size: Int get() = members.size

    final override val isEmpty: Boolean get() = members.isEmpty

    final override fun iterate(block: (E) -> Unit) =
        members.iterate(block)

    final override fun leading(condition: (E) -> Boolean): E? =
        members.leading(condition)

    final override fun has(condition: (E) -> Boolean): Boolean =
        members.has(condition)

    @CallSuper
    override fun add(member: E) {
        check(!members.has { it === member }) {
            "${javaClass.simpleName}: Element is already present"
        }
        members = members.add(member)
    }

    @CallSuper
    override fun remove(member: E) {
        check(members.has { it === member }) {
            "${javaClass.simpleName}: Element is not present"
        }
        members = members.remove(member)
    }
}

internal open class AutoDisposeSwitchGroup<E>(
    private val onDispose: (() -> Unit)? = null
) : SwitchGroup<E>() {

    var isDisposed: Boolean = false
        private set

    @CallSuper
    override fun add(member: E) {
        check(!isDisposed) { "${javaClass.simpleName}: Group is disposed" }
        super.add(member)
    }

    @CallSuper
    override fun remove(member: E) {
        check(!isDisposed) { "${javaClass.simpleName}: Group is disposed" }
        super.remove(member)
        if (isEmpty) dispose()
    }

    @CallSuper
    protected open fun dispose() {
        isDisposed = true
        onDispose?.invoke()
    }
}

private sealed interface SwitchList<out E> {
    val size: Int
    val isEmpty: Boolean
    fun iterate(block: (E) -> Unit)
    fun leading(condition: (E) -> Boolean): E?
    fun has(condition: (E) -> Boolean): Boolean

    object Empty : SwitchList<Nothing> {
        override val size: Int get() = 0
        override val isEmpty: Boolean get() = true
        override fun iterate(block: (Nothing) -> Unit) {}
        override fun leading(condition: (Nothing) -> Boolean): Nothing? = null
        override fun has(condition: (Nothing) -> Boolean): Boolean = false
    }

    class One<E>(val first: E) : SwitchList<E> {
        override val size: Int get() = 1
        override val isEmpty: Boolean get() = false

        override fun iterate(block: (E) -> Unit) =
            block(first)

        override fun leading(condition: (E) -> Boolean): E? =
            when {
                condition(first) -> first
                else -> null
            }

        override fun has(condition: (E) -> Boolean): Boolean =
            condition(first)
    }

    class Two<E>(val first: E, val second: E) : SwitchList<E> {
        override val size: Int get() = 2
        override val isEmpty: Boolean get() = false

        override fun iterate(block: (E) -> Unit) {
            block(first); block(second)
        }

        override fun leading(condition: (E) -> Boolean): E? =
            when {
                condition(first) -> first
                condition(second) -> second
                else -> null
            }

        override fun has(condition: (E) -> Boolean): Boolean =
            condition(first) || condition(second)
    }

    class Three<E>(val first: E, val second: E, val third: E) : SwitchList<E> {
        override val size: Int get() = 3
        override val isEmpty: Boolean get() = false

        override fun iterate(block: (E) -> Unit) {
            block(first); block(second); block(third)
        }

        override fun leading(condition: (E) -> Boolean): E? =
            when {
                condition(first) -> first
                condition(second) -> second
                condition(second) -> third
                else -> null
            }

        override fun has(condition: (E) -> Boolean): Boolean =
            condition(first) || condition(second) || condition(third)
    }

    class Many<E>(first: E, second: E, third: E, fourth: E) : SwitchList<E> {
        val elements = mutableListOf(first, second, third, fourth)

        override val size: Int get() = elements.size
        override val isEmpty: Boolean get() = false

        override fun iterate(block: (E) -> Unit) =
            elements.iterate(block)

        override fun leading(condition: (E) -> Boolean): E? =
            elements.leading(condition)

        override fun has(condition: (E) -> Boolean): Boolean =
            elements.has(condition)
    }
}

private fun <E> SwitchList<E>.add(element: E): SwitchList<E> =
    when (this) {
        is SwitchList.Empty -> SwitchList.One(element)

        is SwitchList.One<E> -> SwitchList.Two(this.first, element)

        is SwitchList.Two<E> ->
            SwitchList.Three(this.first, this.second, element)

        is SwitchList.Three<E> ->
            SwitchList.Many(this.first, this.second, this.third, element)

        is SwitchList.Many<E> -> {
            elements.add(element)
            this
        }
    }

private fun <E> SwitchList<E>.remove(element: E): SwitchList<E> =
    when (this) {
        is SwitchList.Empty -> this

        is SwitchList.One<E> ->
            if (this.first == element) SwitchList.Empty else this

        is SwitchList.Two<E> ->
            when (element) {
                this.first -> SwitchList.One(this.second)
                this.second -> SwitchList.One(this.first)
                else -> this
            }

        is SwitchList.Three<E> ->
            when (element) {
                this.first -> SwitchList.Two(this.second, this.third)
                this.second -> SwitchList.Two(this.first, this.third)
                this.third -> SwitchList.Two(this.first, this.second)
                else -> this
            }

        is SwitchList.Many<E> -> {
            val list = this.elements
            list.remove(element)
            if (list.size == 3) {
                SwitchList.Three(list[0], list[1], list[2])
            } else {
                this
            }
        }
    }