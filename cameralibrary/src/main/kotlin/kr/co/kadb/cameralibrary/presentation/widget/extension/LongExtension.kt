@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import java.text.DecimalFormat

/**
 * Created by oooobang on 2018. 8. 31..
 * Long Extension.
 */
// 콤마를 포함한 숫자형식.
fun Long?.numberWithComma(): String {
	return try {
		DecimalFormat("#,###").format(this)
	} catch (ex: Exception) {
		"0"
	}
}
