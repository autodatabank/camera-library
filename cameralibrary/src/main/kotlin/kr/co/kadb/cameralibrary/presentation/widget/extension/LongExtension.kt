@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Long Extension.
 */
// 콤마를 포함한 숫자형식.
internal fun Long?.toNumberWithComma(): String {
	return try {
		DecimalFormat("#,###").format(this)
	} catch (ex: Exception) {
		"0"
	}
}

// timeInMillis to yyyy-MM-dd 형식 반환.
internal fun Long.toYyyymmdd(hasHyphen: Boolean = false): String = Calendar
	.getInstance()
	.apply {
		timeInMillis = this@toYyyymmdd
	}
	.let {
		if (hasHyphen) {
			SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(it.time)
		} else {
			SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(it.time)
		}
	}

// timeInMillis to yyyy-MM-dd HH:mm:ss.SSS 형식 반환.
internal fun Long.toYyyymmddSSS(): String = Calendar
	.getInstance()
	.apply {
		timeInMillis = this@toYyyymmddSSS
	}
	.let {
		SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA).format(it.time)
	}

// to calendar.
internal fun Long.toCalendar(): Calendar = Calendar.getInstance().apply {
	timeInMillis = this@toCalendar
}

// Elapsed Time(second 초)
internal fun Long.elapsedTime(): String {
	val elapsedTimeInSec = (System.nanoTime() - this).toDouble() / 1000000000.0
	val formattedTime = String.format("%.3f", elapsedTimeInSec)
	return "$formattedTime sec"
}