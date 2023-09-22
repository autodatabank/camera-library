@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StyleRes
import kr.co.kadb.cameralibrary.R
import java.util.*

/**
 * Created by oooobang on 2018. 3. 2..
 * Fragment Extension.
 */
// 키보드 내리기.
internal fun Fragment.hideSoftInput() {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view?.applicationWindowToken, 0)
}

// Show DatePicker.
internal inline fun Fragment.showDatePicker(
    stringDate: String? = null,
    @StyleRes themeResId: Int = android.R.style.Widget_Material_Light_DatePicker,
    crossinline action: (Calendar) -> Unit
) {
    hideSoftInput()
    val calendar = stringDate.toYyyymmdd() ?: Calendar.getInstance()
    context?.let {
        DatePickerDialog(
            it,
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                action.invoke(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
            .show()
    }
}

// Show DatePicker.
internal inline fun Fragment.showTimePicker(
    stringDate: String? = null,
    @StyleRes themeResId: Int = android.R.style.Widget_Material_Light_DatePicker,
    crossinline action: (Calendar) -> Unit
) {
    hideSoftInput()
    val calendar = stringDate.toYyyymmdd() ?: Calendar.getInstance()
    TimePickerDialog(
        context,
        themeResId,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            action.invoke(calendar)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )
        .show()
}

// 전화걸기.
internal fun Fragment.dial(number: String?) {
    when {
        number.isNullOrBlank() -> {
            showAlert(getString(R.string.adb_cameralibrary_text_call_error_string, getString(R.string.adb_cameralibrary_text_nothing)), R.string.adb_cameralibrary_text_notify)
        }
        number.length < 9 -> {
            showAlert(getString(R.string.adb_cameralibrary_text_call_error_string, number), R.string.adb_cameralibrary_text_notify)
        }
        else -> {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            startActivity(intent)
        }
    }
}

// Toast.
internal fun Fragment.showToast(message: CharSequence) {
    activity?.showToast(message)
}

// Toast.
internal fun Fragment.showToast(@StringRes messageId: Int) {
    activity?.showToast(messageId)
}

// 알림 Toast.
internal fun Fragment.showNotificationToast(message: CharSequence) {
    activity?.showNotificationToast(message)
}

// 알림 Toast.
internal fun Fragment.showNotificationToast(@StringRes messageId: Int) {
    activity?.showNotificationToast(messageId)
}

// 성공 Toast.
internal fun Fragment.showSuccessToast(@StringRes messageId: Int) {
    activity?.showSuccessToast(messageId)
}

// 성공 Toast.
internal fun Fragment.showSuccessToast(message: String) {
    activity?.showSuccessToast(message)
}

// 오류 Toast.
internal fun Fragment.showErrorToast(@StringRes messageId: Int) {
    activity?.showErrorToast(messageId)
}

// 오류 Toast.
internal fun Fragment.showErrorToast(message: String) {
    activity?.showErrorToast(message)
}

//
internal fun Fragment.showAlert(message: CharSequence?, @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify) {
    activity?.showAlert(message, titleId)
}

internal fun Fragment.showAlert(@StringRes messageId: Int, @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify) {
    activity?.showAlert(messageId, titleId)
}

internal inline fun Fragment.showAlert(
    message: CharSequence?,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    activity?.showAlert(message, titleId, showAlertDialog)
}

internal inline fun Fragment.showAlert(
    @StringRes messageId: Int,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    activity?.showAlert(messageId, titleId, showAlertDialog)
}

internal inline fun Fragment.showAlert(showAlertDialog: AlertDialog.Builder.() -> Unit) {
    activity?.showAlert(showAlertDialog)
}
