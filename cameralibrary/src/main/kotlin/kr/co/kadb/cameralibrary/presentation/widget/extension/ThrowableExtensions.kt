package kr.co.kadb.cameralibrary.presentation.widget.extension

import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Throwable Extension.
 */

internal fun Throwable.toFailedThrowable(): Throwable {
    // Debug.
    Timber.e(">>>>> Exception : ${this.stackTraceToString()}")

    return when (this) {
        is ConnectException -> Throwable(
            "[${this.javaClass.simpleName}]\n" +
                    "서버와 연결할 수 없습니다. 잠시 후 다시 이용하여 주십시오."
        )
        is SocketTimeoutException -> Throwable(
            "[${this.javaClass.simpleName}]\n" +
                    "연결시간이 초과 되었습니다. 잠시 후 다시 이용하여 주십시오."
        )
        else -> this
    }
}
