@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

/**
 * Created by oooobang on 2018. 2. 22..
 */
fun <E> MutableList<E>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}

fun <E> MutableList<E>?.moveUp(items: List<E>?): MutableList<E>? {
    items?.forEach { target ->
        this?.forEachIndexed { index, item ->
            if (target == item) {
                if (index > 0) {
                    this.swap(index - 1, index)
                }
                return@forEach
            }
        }
    }
    return this
}

fun <E> MutableList<E>?.moveDown(items: List<E>?): MutableList<E>? {
    val size = (this?.size ?: 0) - 1
    items?.reversed()?.forEach { target ->
        this?.reversed()?.forEachIndexed { index, item ->
            if (target == item) {
                if (index < size) {
                    this.swap(index + 1, index)
                }
                return@forEach
            }
        }
    }
    return this
}

fun <E> MutableList<E>?.moveUp(
        items: List<E>?,
        swapAction: (index: Int, target: E, source: E) -> Unit): MutableList<E>? {
    run loop@{
        items?.forEach { target ->
            this?.forEachIndexed { index, item ->
                if (target == item) {
                    if (index > 0) {
                        swapAction(index - 1, target, get(index - 1))
                        this.swap(index - 1, index)
                        return@forEach
                    } else {
                        return@loop
                    }
                }
            }
        }
    }
    return this
}

fun <E> MutableList<E>?.moveDown(
        items: List<E>?,
        swapAction: (index: Int, target: E, source: E) -> Unit): MutableList<E>? {
    val size = this?.size ?: 0//(this?.size ?: 0) - 1
    run loop@{
        items?.reversed()?.forEach { target ->
            this?.reversed()?.forEachIndexed { index, item ->
                if (target == item) {
                    val additionalIndex = size - index - 1
                    if (additionalIndex + 1 < size) {
                        swapAction(additionalIndex + 1, target, get(additionalIndex + 1))
                        this.swap(additionalIndex + 1, additionalIndex)
                        return@forEach
                    } else {
                        return@loop
                    }
                }
            }
        }
    }
    return this
}
