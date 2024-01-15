@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.graphics.Bitmap
import android.util.Base64
import android.util.Patterns
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.math.BigDecimal
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * String Extension.
 */
internal inline fun String.ifNotBlank(block: String.() -> Unit) {
    if (this.isNotBlank()) {
        block(this)
    }
}

internal inline fun String?.ifNotNullOrBlank(block: String.() -> Unit) {
    if (!this.isNullOrBlank()) {
        block(this)
    }
}

// delimiter를 제거 한 문자열이 비어 있지 않으면 원래 문자열을 반환하고 비어 있다면 defaultValue를 반환.
internal inline fun String?.ifBlankBy(removeDelimiter: String, defaultValue: () -> String): String {
    return this?.let {
        if (replace(removeDelimiter, "").isBlank()) {
            defaultValue()
        } else {
            it
        }
    } ?: defaultValue()
}

// delimiter를 제거 한 문자열이 비어 있지 않으면 제거한 문자열을 반환하고 비어 있다면 defaultValue를 반환.
internal inline fun String?.ifRemoveBy(removeDelimiter: String, defaultValue: () -> String): String {
    return this?.let {
        replace(removeDelimiter, "").ifBlank {
            defaultValue()
        }
    } ?: defaultValue()
}

internal inline fun String?.ifZero(block: () -> String): String {
    return this?.let {
        if (isBlank() || trim() == "0") {
            block()
        } else {
            this
        }
    } ?: ""
}

// 차량번호 확인.
internal fun String?.isVehicleNumber(): Boolean {
    return this?.let {
        Regex("(.*[0-9]{2,3}[가-힣][0-9]{4})$").matches(it)
    } ?: false
}

// 광고 접두사 존재 확인.
internal fun String?.hasAdPrefix(): Boolean {
    return this?.trim()?.let {
        it.startsWith("(광고)") || it.startsWith("[광고]")
    } ?: false
}

// 무료수신거부 접두사 존재 확인.
internal fun String?.hasOptOutPrefix(): Boolean {
    return this?.trim()?.contains("무료수신거부") ?: false
}

// 날짜형식 정규식 비교.
internal inline fun String.ifYyyymmdd(defaultValue: String.() -> String): String {
    val yyyymmdd = "(^[0-9]{4}-[0-9]{2}-[0-9]{2}$)|(^[0-9]{4}[0-9]{2}[0-9]{2}\$)"
    return if (Pattern.matches(yyyymmdd, this)) {
        this
    } else {
        defaultValue()
    }
}

// String to Int.
internal fun String?.toIntOrZero(): Int = this?.toIntOrNull() ?: 0

// String to Long.
internal fun String?.toLongOrZero(): Long = this?.toLongOrNull() ?: 0L

// String to BigDecimal.
internal fun String?.toBigDecimalOrZero(): BigDecimal = this?.toBigDecimalOrNull() ?: BigDecimal.ZERO

// String to format As NumberString
internal fun String?.toNumberStringOrZero(): String = this?.toIntOrNull()?.toString() ?: "0"

// 한글인가?
internal fun String?.isKorean(): Boolean {
    return (this?.isNotEmpty() == true && this.all { it in '가'..'힣' })
}

// 첫글자가 한글인가?
internal fun String?.isFirstCharKorean(): Boolean {
    if (this?.isEmpty() == true) return false
    return this?.first() in '가'..'힣'
}

/*// String to BigDecimal.
fun String?.toBigDecimalOrLong(): BigDecimal = this?.toBigDecimalOrNull() ?: BigDecimal.valueOf(0L)

// String to BigDecimal.
fun String?.toBigDecimalOrDouble(): BigDecimal =
    this?.toBigDecimalOrNull() ?: BigDecimal.valueOf(0.0)*/

// String to Calendar.
internal fun String?.toCalendar(pattern: String = "yyyy-MM-dd"): Calendar? = try {
    this?.let {
        val formatter = SimpleDateFormat(pattern, Locale.KOREA)
        val calendar = Calendar.getInstance()
        formatter.parse(this)?.let {
            calendar.time = it
        }
        calendar
    }
} catch (ex: Exception) {
    null
}

internal fun String?.toYyyyMMdd(hasHyphen: Boolean = false): String = try {
    val source = this?.trim()?.replace("-", "") ?: ""
    val formatter = when (hasHyphen) {
        true -> SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        else -> SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    }
    SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        .parse(source)
        ?.let {
            formatter.format(it)
        } ?: ""
} catch (ex: Exception) {
    ""
}

// 전화번호 포멧.
internal fun String?.toPhoneNumber(): String {
    // 전화번호: 하이픈(-) 제거
    val number = this?.trim()?.replace("-", "") ?: ""
    val lastNumber = number.takeLast(4)

    // 15771577     => 8
    // 021231234    => 9
    // 0212341234   => 10
    // 0321231234   => 10
    // 03212341234  => 11
    return if (number.length == 9) {
        "${number.take(2)}-${number.slice(2..4)}-$lastNumber"
    } else if (number.length == 10 && number.startsWith("02")) {
        "${number.take(2)}-${number.slice(2..5)}-$lastNumber"
    } else if (number.length == 10) {
        "${number.take(3)}-${number.slice(3..5)}-$lastNumber"
    } else if (number.length > 10) {
        "${number.dropLast(8)}-${number.slice(3..6)}-$lastNumber"
    } else if (number.length > 4) {
        "${number.dropLast(4)}-$lastNumber"
    } else {
        lastNumber
    }
}

// 전화번호 '-' 하이픈 분리.
internal fun String?.toPhoneNumberWithTriple(): Triple<String, String, String> {
    // 전화번호: 하이픈(-) 제거
    val number = this?.trim()?.replace("-", "") ?: ""
    val lastNumber = number.takeLast(4)

    // 15771577     => 8
    // 021231234    => 9
    // 0212341234   => 10
    // 0321231234   => 10
    // 03212341234  => 11
    return if (number.length == 9) {
        Triple(number.take(2), number.slice(2..4), lastNumber)
    } else if (number.length == 10 && number.startsWith("02")) {
        Triple(number.take(2), number.slice(2..5), lastNumber)
    } else if (number.length == 10) {
        Triple(number.take(3), number.slice(3..5), lastNumber)
    } else if (number.length > 10) {
        Triple(number.dropLast(8), number.slice(3..6), lastNumber)
    } else if (number.length > 4) {
        Triple("", number.dropLast(4), lastNumber)
    } else {
        Triple("", "", lastNumber)
    }
}

// 사업자번호 포멧.
internal fun String?.toBusinessNumber(): String {
    // 사업자번호: 하이픈(-) 제거
    val number = this?.trim()?.replace("-", "") ?: ""
    val lastNumber = number.takeLast(5)

    // 1231212345     => 10
    return if (number.length > 7) {
        "${number.dropLast(7)}-${number.slice(3..4)}-$lastNumber"
    } else if (number.length > 5) {
        "${number.dropLast(5)}-$lastNumber"
    } else {
        lastNumber
    }
}

// 사업자번호 '-' 하이픈 분리.
internal fun String?.toBusinessNumberWithTriple(): Triple<String, String, String> {
    // 사업자번호: 하이픈(-) 제거
    val number = this?.trim()?.replace("-", "") ?: ""
    val lastNumber = number.takeLast(5)

    // 1231212345     => 10
    return if (number.length > 7) {
        Triple(number.dropLast(7), number.slice(3..4), lastNumber)
    } else if (number.length > 5) {
        Triple("", number.dropLast(5), lastNumber)
    } else {
        Triple("", "", lastNumber)
    }
}

// Email.
internal fun String?.toEmailWithPair(): Pair<String, String> {
    listOf("", "").withIndex()
    return this?.split("@")?.let {
        Pair(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "")
    } ?: Pair(this ?: "", "")
}

// 이메일 유효성.
internal fun String.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

// 콤마를 포함한 숫자형식.
internal fun String?.toNumberWithComma(): String {
    return if (this?.isBlank() == true) {
        "0"
    } else {
        try {
            DecimalFormat("#,###").format(this?.toLong())
        } catch (ex: Exception) {
            this ?: "0"
        }
    }
}

// 콤마(,) 마침표(.) 제거.
internal fun String?.removeCurrency(): String {
    return try {
        this?.replace("""[,.]""".toRegex(), "") ?: ""
    } catch (ex: Exception) {
        ""
    }
}

// 공백 제거.
internal fun String?.removeBlanks(): String {
    return try {
        this?.replace(" ", "") ?: ""
    } catch (ex: Exception) {
        ""
    }
}

// 소숫점 '0' 제거.
internal fun String?.removeDecimalZero() = try {
    this?.toBigDecimalOrZero()?.stripTrailingZeros()?.toPlainString() ?: "0"
} catch (ex: Exception) {
    "0"
}

// Remove URL.
internal fun String.removeUrl(): String {
    val urlPattern =
        "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?+-=\\\\.&]*)"
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
internal fun String.change(
    filename: String? = null,
    extension: String? = null,
    format: Bitmap.CompressFormat? = null
): String {
    val name = filename ?: this.split(".").first()
    var exte = extension ?: this.split(".").last()
    format?.let {
        exte = when (it) {
            Bitmap.CompressFormat.PNG -> "png"
            Bitmap.CompressFormat.JPEG -> "jpeg"
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

// Base64.
internal fun String?.toBase64(
    flags: Int = Base64.NO_WRAP
): String = if (this.isNullOrBlank()) {
    ""
} else {
    Base64.encodeToString(this.toByteArray(), flags)
}

// URLEncoder.
internal fun String?.urlEncoder(encoding: String = "UTF-8") = this?.let {
    URLEncoder.encode(it, encoding)
} ?: ""

// URLDecoder.
internal fun String?.urlDecoder(decoding: String = "UTF-8") = this?.let {
    URLDecoder.decode(it, decoding)
} ?: ""

// md5
internal fun String.md5(): String {
    return hashString(this, "MD5")
}

// sha256
internal fun String.sha256(): String {
    return hashString(this, "SHA-256")
}

private fun hashString(input: String, algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

internal fun String?.ensureFileSchema(): String? {
    return if (this == null) {
        null
    } else if (this.startsWith("content://")) {
        // "content://" 스키마가 이미 포함되어 있는지 확인.
        this
    } else if (this.startsWith("file://")) {
        // "file://" 스키마가 이미 포함되어 있는지 확인
        this
    } else {
        // 스키마가 포함되어 있지 않으면 추가
        return "file://$this"
    }
}
