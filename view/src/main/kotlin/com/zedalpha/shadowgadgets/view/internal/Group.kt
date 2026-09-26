package com.zedalpha.shadowgadgets.view.internal

import androidx.annotation.CallSuper

internal interface Group<E> {
    val size: Int
    fun isEmpty(): Boolean
    fun add(element: E)
    fun remove(element: E)
    fun iterate(block: (E) -> Unit)
    fun find(condition: (E) -> Boolean): E?
    fun has(condition: (E) -> Boolean): Boolean
}

@JvmInline
internal value class ListGroup<E>
private constructor(private val list: MutableList<E>) : Group<E> {

    constructor() : this(mutableListOf())

    override val size: Int get() = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun add(element: E) {
        check(element !in list) { "Element is already present" }
        list.add(element)
    }

    override fun remove(element: E) =
        check(list.remove(element)) { "Element is not present" }

    override fun iterate(block: (E) -> Unit) = list.iterate(block)

    override fun find(condition: (E) -> Boolean): E? = list.find(condition)

    override fun has(condition: (E) -> Boolean): Boolean = list.has(condition)
}

internal open class AutoDisposeListGroup<E>
private constructor(private val group: ListGroup<E>) : Group<E> {

    constructor() : this(ListGroup())

    // Checked only in mutators.
    var isDisposed: Boolean = false
        private set

    final override val size: Int get() = group.size

    final override fun isEmpty(): Boolean = group.isEmpty()

    final override fun add(element: E) {
        check(!isDisposed) { "${javaClass.simpleName} is disposed" }
        group.add(element)
    }

    final override fun remove(element: E) {
        check(!isDisposed) { "${javaClass.simpleName} is disposed" }
        group.remove(element)
        if (isEmpty()) dispose()
    }

    final override fun iterate(block: (E) -> Unit) =
        group.iterate(block)

    final override fun find(condition: (E) -> Boolean): E? =
        group.find(condition)

    final override fun has(condition: (E) -> Boolean): Boolean =
        group.has(condition)

    @CallSuper
    protected open fun dispose() {
        isDisposed = true
    }
}