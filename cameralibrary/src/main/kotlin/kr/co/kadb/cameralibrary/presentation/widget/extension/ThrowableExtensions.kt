package kr.co.kadb.cameralibrary.presentation.widget.extension

import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog
import java.net.*

/**
 * Throwable Extension.
 */
internal fun Throwable.toFailedThrowable(): Throwable {
    // Debug.
    DebugLog.e { ">>>>> Exception : ${this.stackTraceToString()}" }

    return when (this) {
        is ConnectException -> Throwable(
            "[${this.javaClass.simpleName}]\n" +
                    "서버와 연결할 수 없습니다. 잠시 후 다시 이용하여 주십시오."
        )
        is SocketTimeoutException -> Throwable(
            "[${this.javaClass.simpleName}]\n" +
                    "연결시간이 초과 되었습니다. 잠시 후 다시 이용하여 주십시오."
        )
        is UnknownHostException -> Throwable(
            "[${this.javaClass.simpleName}]\n" +
                    "네트워크 연결에 문제가 발생하였습니다.\n인터넷 환경(LTE, 5G, WiFi)을 점검 후 다시 이용하여 주십시오."
        )
        else -> this
    }
}
