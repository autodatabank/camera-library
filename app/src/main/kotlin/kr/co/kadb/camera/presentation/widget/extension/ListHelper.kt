@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

/**
 * Created by oooobang on 2018. 2. 22..
 */
internal val <E> List<E>?.first: E?
	get() = if (this?.isNotEmpty() == true) {
		this[0]
	} else {
		null
	}
