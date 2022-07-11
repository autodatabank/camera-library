@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.widget.EditText
import android.widget.ScrollView

/**
 * Created by oooobang on 2018. 4. 1..
 */
internal fun ScrollView.debug() {
	setOnTouchListener { _, _ ->
		if (focusedChild is EditText) {
			val edittext = focusedChild as EditText
			if (edittext.hasFocus()) {
				edittext.hideSoftInput()
				edittext.clearFocus()
			}
		}
//			v.clearFocus()
		false
	}
}