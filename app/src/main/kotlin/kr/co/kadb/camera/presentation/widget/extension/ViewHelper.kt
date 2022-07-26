@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.view.View

/**
 * Created by oooobang on 2018. 3. 7..
 * View Extension.
 */

internal fun View.showSoftInput() {
	context.inputMethodManager.showSoftInput(this, 0)
}

internal fun View.hideSoftInput() {
	context.inputMethodManager.hideSoftInputFromWindow(this.applicationWindowToken, 0)
}