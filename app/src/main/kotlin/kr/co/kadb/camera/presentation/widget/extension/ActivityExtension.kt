@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import kr.co.kadb.cameralibrary.R

/**
 * Created by oooobang on 2018. 3. 2..
 * Activity Extension.
 */

// toast.
internal var toast: Toast? = null

// 키보드 내리기.
fun Activity.hideSoftInput() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(window?.decorView?.rootView?.applicationWindowToken, 0)
}

// Toast.
@SuppressLint("ShowToast")
fun Activity.showToast(message: CharSequence) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast?.show()
}

// Toast.
@SuppressLint("ShowToast")
fun Activity.showToast(@StringRes messageId: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT)
    toast?.show()
}

// 알림 Toast.
@SuppressLint("ShowToast")
fun Activity.showNotificationToast(message: CharSequence) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast?.show()
}

// 알림 Toast.
@SuppressLint("ShowToast")
fun Activity.showNotificationToast(@StringRes messageId: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT)
    toast?.show()
}

// 성공 Toast.
@SuppressLint("ShowToast")
fun Activity.showSuccessToast(@StringRes messageId: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT)
    toast?.show()
}

// 성공 Toast.
@SuppressLint("ShowToast")
fun Activity.showSuccessToast(message: String) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast?.show()
}

// 오류 Toast.
@SuppressLint("ShowToast")
fun Activity.showErrorToast(@StringRes messageId: Int) {
    toast?.cancel()
    toast = Toast.makeText(this, messageId, Toast.LENGTH_SHORT)
    toast?.show()
}

// 오류 Toast.
@SuppressLint("ShowToast")
fun Activity.showErrorToast(message: String) {
    toast?.cancel()
    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast?.show()
}

fun Activity.showAlert(message: CharSequence?, @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify) {
    showAlert {
        setTitle(titleId)
        setMessage(message)
        positiveButton {}
    }
}

fun Activity.showAlert(@StringRes messageId: Int, @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify) {
    showAlert {
        setTitle(titleId)
        setMessage(messageId)
        positiveButton {}
    }
}

inline fun Activity.showAlert(message: CharSequence?,
                                       @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
                                       showAlertDialog: AlertDialog.Builder.() -> Unit) {
    if (!this.isFinishing) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(R.string.adb_cameralibrary_text_confirm, null)
        dialogBuilder.show()
    }
}

inline fun Activity.showAlert(@StringRes messageId: Int,
                                       @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
                                       showAlertDialog: AlertDialog.Builder.() -> Unit) {
    if (!this.isFinishing) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(messageId)
        dialogBuilder.setPositiveButton(R.string.adb_cameralibrary_text_confirm, null)
        dialogBuilder.show()
    }
}

inline fun Activity.showAlert(showAlertDialog: AlertDialog.Builder.() -> Any) {
    if (!this.isFinishing) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.show()
    }
}
