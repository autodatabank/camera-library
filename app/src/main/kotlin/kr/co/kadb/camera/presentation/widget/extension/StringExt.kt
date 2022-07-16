@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


/**
 * Created by oooobang on 2018. 3. 13..
 * String Extension.
 */
// String to Calendar.
internal fun String?.toBigDecimalOrDouble(): BigDecimal = this?.toBigDecimalOrNull()
        ?: BigDecimal.valueOf(0.0)

// String to Calendar.
internal fun String?.yyyymmdd(): String? {
    return if ((this?.length ?: 0) >= 10) {
        this?.substring(0, 10)
    } else {
        this
    }
}

// String to Calendar.
internal fun String?.toYyyymmdd(): Calendar? = try {
    this?.let {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        val calendar = Calendar.getInstance()
        formatter.parse(this)?.let {
            calendar.time = it
        }
        calendar
    }
} catch (ex: Exception) {
    null
}

// 전화번호 '-' 하이픈 추가.
internal fun String?.addHyphenPhoneNumber(): String {
    return this?.let {
        when (it.length) {
			8 -> it.replaceFirst("^([0-9]{4})([0-9]{4})$".toRegex(), "$1-$2")
			12 -> it.replaceFirst("(^[0-9]{4})([0-9]{4})([0-9]{4})$".toRegex(), "$1-$2-$3")
            else -> it.replaceFirst("(^02|[0-9]{3})([0-9]{3,4})([0-9]{4})$".toRegex(), "$1-$2-$3")
        }
    } ?: ""
}

// 전화번호 '-' 하이픈 분리.
internal fun String?.splitPhoneNumber(): Array<String?> {
    val numbers = arrayOfNulls<String>(3)
    this?.split("-")?.forEachIndexed { index, s ->
        try {
            numbers[index] = s
        } catch (ex: Exception) {
        }
    }
    return numbers
}

// Email.
internal fun String?.splitEmail(): Array<String?> {
    val emails = arrayOfNulls<String>(2)
    this?.split("@")?.forEachIndexed { index, s ->
        try {
            emails[index] = s
        } catch (ex: Exception) {
        }
    }
    return emails
}

// Json Pretty.
internal fun String?.toJsonPretty(): String {
    val json = JsonParser.parseString(this).asJsonObject
    val gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(json)
}

// 콤마를 포함한 숫자형식.
internal fun String?.numberWithComma(): String {
    return try {
        DecimalFormat("#,###").format(this?.toLong())
    } catch (ex: Exception) {
        "0"
    }
}

// 콤마 제거.
internal fun String?.removeCommas(): String {
    return try {
        this?.replace("[,]".toRegex(), "") ?: ""
    } catch (ex: Exception) {
        ""
    }
}

// 소숫점 '0' 제거.
internal fun String?.removeDecimalZero() = try {
    (this?.toDoubleOrNull() ?: "0").toString()
} catch (ex: Exception) {
    "0"
}

// Remove URL.
internal fun String.removeUrl(): String {
    val urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
    val pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    var i = 0
    var result = ""
    while (matcher.find()) {
        matcher.group(i)?.toRegex()?.let { regex ->
            result = this.replace(regex, "").trim { it <= ' ' }
        }
        i++
    }
    return result
}

// 파일명, 확장자명 변경.
internal fun String.change(filename: String? = null,
				  extension: String? = null,
				  format: Bitmap.CompressFormat? = null): String {
    val name = filename ?: this.split(".").first()
    var exte = extension ?: this.split(".").last()
    format?.let {
        exte = when (it) {
            Bitmap.CompressFormat.PNG -> "png"
			Bitmap.CompressFormat.JPEG -> "jpg"
			else -> "webp"
        }
    }
    return "$name.$exte"
}

// toNotNull.
internal fun String?.toNotNull(): String {
    return this ?: ""
}

// toNotNull.
internal fun String?.equalsBlank(other: String?) = if (isNullOrBlank() && other.isNullOrBlank()) {
    true
} else this == other