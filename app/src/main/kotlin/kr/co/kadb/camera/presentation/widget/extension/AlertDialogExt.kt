@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import kr.co.kadb.camera.R

/**
 * Created by oooobang on 2018. 4. 30..
 * AlertDialog Extension.
 */

//
fun AlertDialog.Builder.positiveButton(@StringRes title: Int = R.string.text_adb_camera_confirm, listener: () -> Unit) {
	this.setPositiveButton(title) { _, _ ->
		listener.invoke()
	}
}

fun AlertDialog.Builder.negativeButton(@StringRes title: Int = R.string.text_adb_camera_cancel, listener: () -> Unit) {
	this.setNegativeButton(title) { _, _ ->
		listener.invoke()
	}
}

fun AlertDialog.Builder.neutralButton(@StringRes title: Int = R.string.text_adb_camera_cancel, listener: () -> Unit) {
	this.setNeutralButton(title) { _, _ ->
		listener.invoke()
	}
}

fun AlertDialog.Builder.cancelButton(@StringRes title: Int = R.string.text_adb_camera_cancel, listener: () -> Unit) {
	this.setNegativeButton(title) { _, _ ->
		listener.invoke()
	}
}
