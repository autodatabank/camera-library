@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import kr.co.kadb.cameralibrary.R

/**
 * Created by oooobang on 2018. 4. 30..
 * AlertDialog Extension.
 */

//
internal fun AlertDialog.Builder.positiveButton(@StringRes title: Int = R.string.adb_cameralibrary_text_confirm, listener: () -> Unit) {
	this.setPositiveButton(title) { _, _ ->
		listener.invoke()
	}
}

internal fun AlertDialog.Builder.negativeButton(@StringRes title: Int = R.string.adb_cameralibrary_text_cancel, listener: () -> Unit) {
	this.setNegativeButton(title) { _, _ ->
		listener.invoke()
	}
}

internal fun AlertDialog.Builder.neutralButton(@StringRes title: Int = R.string.adb_cameralibrary_text_cancel, listener: () -> Unit) {
	this.setNeutralButton(title) { _, _ ->
		listener.invoke()
	}
}

internal fun AlertDialog.Builder.cancelButton(@StringRes title: Int = R.string.adb_cameralibrary_text_cancel, listener: () -> Unit) {
	this.setNegativeButton(title) { _, _ ->
		listener.invoke()
	}
}
