@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import kr.co.kadb.camera.R
import kr.co.kadb.camera.presentation.widget.util.IntentKey.BROADCAST_FINISH
import kr.co.kadb.camera.presentation.widget.util.OBMediaScanning
import java.io.File

/**
 * Created by oooobang on 2018. 3. 7..
 * Context Extension.
 */
// 어플리케이션 종료.
internal fun Context.applicationFinish() {
    showAlert {
        setTitle(R.string.text_adb_camera_notify)
        setMessage(R.string.text_adb_camera_application_finish)
        positiveButton {
            sendBroadcast(Intent(BROADCAST_FINISH))
        }
        cancelButton {}
    }
}

internal fun Context.getResourceIndex(@ArrayRes resourceId: Int, text: String?): Int {
    val resources = resources.getStringArray(resourceId)
    resources.forEachIndexed { index, s ->
        if (s == text) {
            return index
        }
    }
    return 0
}

//
internal fun Context.getResourceString(@ArrayRes resourceId: Int, index: Int): String {
    return resources.getStringArray(resourceId)[index]
}

// AlertDialog.
internal fun Context.showAlert(message: CharSequence?, @StringRes titleId: Int = R.string.text_adb_camera_notify) {
    showAlert {
        setTitle(titleId)
        setMessage(message)
    }
}

// AlertDialog.
internal fun Context.showAlert(@StringRes messageId: Int, @StringRes titleId: Int = R.string.text_adb_camera_notify) {
    showAlert {
        setTitle(titleId)
        setMessage(messageId)
    }
}

// AlertDialog.
internal inline fun Context.showAlert(
    message: CharSequence?,
    @StringRes titleId: Int = R.string.text_adb_camera_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    if ((this as? Activity)?.isFinishing != true) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(R.string.text_adb_camera_confirm, null)
        dialogBuilder.show()
    }
}

// AlertDialog.
internal inline fun Context.showAlert(
    @StringRes messageId: Int,
    @StringRes titleId: Int = R.string.text_adb_camera_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    if ((this as? Activity)?.isFinishing != true) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(messageId)
        dialogBuilder.setPositiveButton(R.string.text_adb_camera_confirm, null)
        dialogBuilder.show()
    }
}

// AlertDialog.
internal inline fun Context.showAlert(showAlertDialog: AlertDialog.Builder.() -> Any) {
    if ((this as? Activity)?.isFinishing != true) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.show()
    }
}

// Media Scanning
internal fun Context.mediaScanning(file: File) = OBMediaScanning(this, file)

// Media Scanning
internal fun Context.mediaScanning(file: String) = OBMediaScanning(this, File(file))

// InputMethodManager.
internal val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

/**
 * Generic registerReceiver extension to reduce boilerplate
 *
 * Call this like so:
 * val myReceiver = registerReceiver(IntentFilter(BROADCAST_SOMETHING_HAPPENED)) {
 *     when (intent?.action) {
 *         BROADCAST_SOMETHING_HAPPENED -> handleSomethingHappened()
 *     }
 * }
 *
 * Call this extension from your Activity's onStart(), keep a reference
 * to the returned receiver and unregister it in onStop()
 *
 * Note: If you support devices on Honeycomb or earlier,
 * then you must call this in onResume() and unregister in onPause()
 */
internal inline fun Context.registerReceiver(
    intentFilter: IntentFilter,
    crossinline onReceive: (intent: Intent?) -> Unit
): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            onReceive.invoke(intent)
        }
    }
    registerReceiver(receiver, intentFilter)
    return receiver
}
