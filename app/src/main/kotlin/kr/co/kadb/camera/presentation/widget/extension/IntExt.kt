@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import java.text.DecimalFormat

/**
 * Created by oooobang on 2018. 8. 31..
 * String Extension.
 */
// 콤마를 포함한 숫자형식.
fun Int?.numberWithComma(): String {
	return try {
		DecimalFormat("#,###").format(this)
	} catch (ex: Exception) {
		"0"
	}
}

// toNotNull.
fun Int?.toNotNull(): Int {
	return this ?: 0
}
