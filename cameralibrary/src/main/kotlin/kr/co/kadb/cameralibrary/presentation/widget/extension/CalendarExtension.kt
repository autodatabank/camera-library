@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Extension.
 */
// Calendar to형식 반환.
internal fun Calendar.toStringBy(format: String): String = try {
	SimpleDateFormat(format, Locale.KOREA).format(time)
} catch (ex: Exception) {
	""
}

// Calendar to timeInMillis.
internal fun Calendar.toDayUntilMillis(): Long {
	return Calendar.getInstance().apply {
		clear()
		set(
			this@toDayUntilMillis.get(Calendar.YEAR),
			this@toDayUntilMillis.get(Calendar.MONTH),
			this@toDayUntilMillis.get(Calendar.DAY_OF_MONTH)
		)
	}.timeInMillis
}

// 시, 분, 초, ms 초기화.
internal fun Calendar.clearUnderDay(): Calendar {
	return Calendar.getInstance().apply {
		clear()
		set(
			this@clearUnderDay.get(Calendar.YEAR),
			this@clearUnderDay.get(Calendar.MONTH),
			this@clearUnderDay.get(Calendar.DAY_OF_MONTH)
		)
	}
}

// Calendar to yyyy-MM-dd HH:mm:ss.SSS 형식 반환.
internal fun Calendar.toYyyymmddSSS(hasDivision: Boolean = false): String {
	val formatter = if (hasDivision) {
		SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA)
	} else {
		SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.KOREA)
	}
	return formatter.format(time)
}

// Calendar to yyyy-MM-dd HH:mm:ss 형식 반환.
internal fun Calendar.toYyyymmddss(): String {
	val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
	return formatter.format(time)
}

// Calendar to yyyy-MM-dd 형식 반환.
internal fun Calendar.toYyyymmdd(hasHyphen: Boolean = false): String {
	val formatter = if (hasHyphen) {
		SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
	} else {
		SimpleDateFormat("yyyyMMdd", Locale.KOREA)
	}
	return formatter.format(time)
}

// Calendar to yyyyMM 형식 반환.
internal fun Calendar.toYyyymm(): String {
	val formatter = SimpleDateFormat("yyyyMM", Locale.KOREA)
	return formatter.format(time)
}

// Calendar to HH:mm:ss 형식 반환.
internal fun Calendar.toHhmmss(hasDivision: Boolean = false): String {
	val formatter = if (hasDivision) {
		SimpleDateFormat("HH:mm:ss", Locale.KOREA)
	} else {
		SimpleDateFormat("HHmmss", Locale.KOREA)
	}
	return formatter.format(time)
}

// Calendar to HH:mm 형식 반환.
internal fun Calendar.toHhmm(): String {
	val formatter = SimpleDateFormat("HH:mm", Locale.KOREA)
	return formatter.format(time)
}

internal fun Calendar.beginDayOfMonth(value: Int = 7, hasHyphen: Boolean = false): String {
	val formatter = if (hasHyphen) {
		SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
	} else {
		SimpleDateFormat("yyyyMMdd", Locale.KOREA)
	}
	set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) - value)
	return formatter.format(time)
}

internal fun Calendar.endDayOfMonth(value: Int = 7, hasHyphen: Boolean = false): String {
	val formatter = if (hasHyphen) {
		SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
	} else {
		SimpleDateFormat("yyyyMMdd", Locale.KOREA)
	}
	set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH) + value)
	return formatter.format(time)
}