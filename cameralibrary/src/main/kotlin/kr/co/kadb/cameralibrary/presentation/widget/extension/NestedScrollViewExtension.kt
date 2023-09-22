@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import androidx.core.widget.NestedScrollView

/**
 * Created by oooobang on 2020. 1. 28..
 * NestedScrollView Extension.
 */
internal inline fun NestedScrollView.setOnScrollTopListener(crossinline changed: () -> Unit) {
	setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
		if (scrollY == 0) {
			changed.invoke()
		}
	}
}