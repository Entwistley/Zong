package com.xenoage.utils

/**
 * Like [forEach], but in reversed order.
 */
fun <T> List<T>.forEachReversed(action: (T) -> Unit) {
	var it = listIterator(size)
	while (it.hasPrevious())
		action(it.previous())
}

/**
 * Returns a new mutable list only of those given elements, that are not null.
 */
fun <T> mutableListOfNotNull(vararg elements: T?): MutableList<T> {
	val ret = ArrayList<T>(elements.size)
	elements.forEach { if (it != null) ret.add(it) }
	return ret
}

/**
 * Sets the element at the given index in this list. If the index is out of
 * the bounds of this list, it is extended up to this index
 * and the gaps are filled with the given fillElement.
 */
fun <T> MutableList<T>.setExtend(index: Int, element: T, fillElement: T) {
	while (index >= size)
		add(fillElement)
	this[index] = element
}