@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import java.text.DecimalFormat

/**
 * Created by oooobang on 2018. 8. 31..
 * Int Extension.
 */
// 콤마를 포함한 숫자형식.
internal fun Int?.toNumberWithComma(): String {
	return try {
		DecimalFormat("#,###").format(this)
	} catch (ex: Exception) {
		"0"
	}
}

// toNotNull.
internal fun Int?.toNotNull(): Int {
	return this ?: 0
}

// convert to bit format.
internal fun Int.to32bitString(): String {
	return Integer.toBinaryString(this).padStart(Int.SIZE_BITS, '0')
}

// 비트연산 하나라도 포함.
internal fun Int.contains(vararg states: Int) = states.any {
	this and it != 0x00000000
}

// 비트연산 모두 포함.
internal fun Int.containsAll(vararg states: Int) = states.reduce { accumulator, state ->
	accumulator or state
}.let {
	this and it == it
}

// 비트연산 하나라도 포함하지 않음.
internal fun Int.notContains(vararg states: Int) = states.none {
	this and it != 0x00000000
}

// 비트연산 모두 포함하지 않음.
internal fun Int.notContainsAll(vararg states: Int) = states.reduce { accumulator, state ->
	accumulator or state
}.let {
	this and it != it
}