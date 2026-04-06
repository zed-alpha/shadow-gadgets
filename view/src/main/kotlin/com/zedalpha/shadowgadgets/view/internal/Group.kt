package com.zedalpha.shadowgadgets.view.internal

import androidx.annotation.CallSuper

internal interface Group<E> {
    val size: Int
    fun isEmpty(): Boolean
    fun add(element: E)
    fun remove(element: E)
    fun iterate(block: (E) -> Unit)
    fun has(condition: (E) -> Boolean): Boolean
    fun find(condition: (E) -> Boolean): E?
}

@JvmInline
internal value class ListGroup<E>
private constructor(private val list: MutableList<E>) : Group<E> {

    constructor() : this(mutableListOf())

    override val size: Int get() = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun add(element: E) =
        check(list.add(element)) { "Element is already present" }

    override fun remove(element: E) =
        check(list.remove(element)) { "Element is not present" }

    override fun iterate(block: (E) -> Unit) = list.iterate(block)

    override fun find(condition: (E) -> Boolean): E? = list.find(condition)

    override fun has(condition: (E) -> Boolean): Boolean = list.has(condition)
}

internal open class AutoDisposeListGroup<E>
private constructor(private val group: ListGroup<E>) : Group<E> by group {

    constructor() : this(ListGroup())

    var isDisposed: Boolean = false
        private set

    @CallSuper
    override fun add(element: E) {
        check(!isDisposed) { "${javaClass.simpleName} is disposed" }
        group.add(element)
    }

    @CallSuper
    override fun remove(element: E) {
        check(!isDisposed) { "${javaClass.simpleName} is disposed" }
        group.remove(element)
        if (isEmpty()) dispose()
    }

    @CallSuper
    protected open fun dispose() {
        isDisposed = true
    }
}