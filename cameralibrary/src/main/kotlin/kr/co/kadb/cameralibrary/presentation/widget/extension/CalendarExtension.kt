@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by oooobang on 2018. 3. 29..
 * Calendar Extension.
 */
// Calendar to형식 반환.
internal fun Calendar.toFormatter(format: SimpleDateFormat): String  {
	return format.format(time)
}

// Calendar to YYYY-MM-DD 형식 반환.
internal fun Calendar.toYyyymmddss(): String  {
	val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN)
	return formatter.format(time)
}

// Calendar to YYYY-MM-DD 형식 반환.
internal fun Calendar.toYyyymmdd(isHyphen: Boolean = false): String  {
	val formatter = if (isHyphen) {
		SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
	} else {
		SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
	}
	return formatter.format(time)
}

// Calendar to YYYYMM 형식 반환.
internal fun Calendar.toYyyymm(): String  {
	val formatter = SimpleDateFormat("yyyyMM", Locale.KOREAN)
	return formatter.format(time)
}

// Calendar to HH:mm 형식 반환.
internal fun Calendar.toHhmm(): String  {
	val formatter = SimpleDateFormat("HH:mm", Locale.KOREAN)
	return formatter.format(time)
}