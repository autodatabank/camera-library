@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.widget.TextView

/**
 * TextView Extension.
 */
// 입력 문자열 가져오기.
internal fun TextView?.getString() = this?.text?.toString() ?: ""

// 입력 문자열 가져오기.
internal fun TextView?.getStringByTrim() = this?.text?.trim()?.toString() ?: ""

// 입력 문자열 가져오기.
internal fun TextView?.getNumberStringOrZero() = this?.text?.trim()?.toString().toNumberStringOrZero()
