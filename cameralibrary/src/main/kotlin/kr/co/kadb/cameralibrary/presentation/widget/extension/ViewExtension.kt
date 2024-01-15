@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.view.View

/**
 * View Extension.
 */
internal fun View.showSoftInput() {
	context?.inputMethodManager?.showSoftInput(this, 0)
}

internal fun View.hideSoftInput() {
	context?.inputMethodManager?.hideSoftInputFromWindow(this.applicationWindowToken, 0)
}