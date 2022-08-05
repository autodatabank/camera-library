@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.view.View

/**
 * Created by oooobang on 2018. 3. 7..
 * View Extension.
 */

fun View.showSoftInput() {
	context.inputMethodManager.showSoftInput(this, 0)
}

fun View.hideSoftInput() {
	context.inputMethodManager.hideSoftInputFromWindow(this.applicationWindowToken, 0)
}