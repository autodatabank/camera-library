@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import java.text.DecimalFormat

/**
 * Created by oooobang on 2018. 8. 31..
 * String Extension.
 */
// 콤마를 포함한 숫자형식.
internal fun Double?.numberWithComma(): String {
	return try {
		DecimalFormat("#,###").format(this)
	} catch (ex: Exception) {
		"0"
	}
}

// toNotNull.
internal fun Double?.toNotNull(): Double {
	return this ?: 0.0
}
