@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ImageCapture
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.BROADCAST_FINISH
import kr.co.kadb.cameralibrary.presentation.widget.util.OBMediaScanning
import timber.log.Timber
import java.io.File

/**
 * Created by oooobang on 2018. 3. 7..
 * Context Extension.
 */
// 어플리케이션 종료.
internal fun Context.applicationFinish() {
    showAlert {
        setTitle(R.string.adb_cameralibrary_text_notify)
        setMessage(R.string.adb_cameralibrary_text_application_finish)
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
internal fun Context.showAlert(
    message: CharSequence?,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify
) {
    showAlert {
        setTitle(titleId)
        setMessage(message)
    }
}

// AlertDialog.
internal fun Context.showAlert(
    @StringRes messageId: Int,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify
) {
    showAlert {
        setTitle(titleId)
        setMessage(messageId)
    }
}

// AlertDialog.
internal inline fun Context.showAlert(
    message: CharSequence?,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    if ((this as? Activity)?.isFinishing != true) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(message)
        dialogBuilder.setPositiveButton(R.string.adb_cameralibrary_text_confirm, null)
        dialogBuilder.show()
    }
}

// AlertDialog.
internal inline fun Context.showAlert(
    @StringRes messageId: Int,
    @StringRes titleId: Int = R.string.adb_cameralibrary_text_notify,
    showAlertDialog: AlertDialog.Builder.() -> Unit
) {
    if ((this as? Activity)?.isFinishing != true) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.showAlertDialog()
        dialogBuilder.create()
        dialogBuilder.setTitle(titleId)
        dialogBuilder.setMessage(messageId)
        dialogBuilder.setPositiveButton(R.string.adb_cameralibrary_text_confirm, null)
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

// 파일생성.
internal fun Context?.createFile(
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): File? {
    var path: String? = null
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
        else -> "webp"
    }

    if (isPublicDirectory) {
        // 하위 디렉토리명.
        val childDirectory = this?.packageName?.split('.')?.last() ?: "adbcamerax"
        // 공유 저장소 사용 시 Android Q 대응.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.$extension")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$childDirectory"
                )
            }

            this?.contentResolver?.let { contentResolver ->
                // ContentResolver을 통해 insert.
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                contentResolver.insert(collection, contentValues)?.let { uri ->
                    // Debug.
                    Timber.i(">>>>> VERSION_CODES >= Q URI : %s", uri)

                    // 반환용.
                    path = uri.toString()
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                childDirectory
            )
            if (!directory.mkdirs()) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }

            path = "${directory.absolutePath}/$filename.$extension"

            // Debug.
            Timber.i(">>>>> VERSION_CODES < Q PATH : %s", path)
        }
    } else {
        // 내부 저장소 사용.
        val directory = this?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory?.mkdirs() != true) {
            Timber.i(">>>>> Directory not created : %s", directory)
        }
        path = "${directory?.absolutePath}/$filename.$extension"

        // Debug.
        Timber.i(">>>>> PATH : %s", path)
    }

    return path?.let {
        File(it)
    }
}

// OutputFileOptions.Builder 생성.
internal fun Context.outputFileOptionsBuilder(
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): ImageCapture.OutputFileOptions.Builder {
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
        else -> "webp"
    }

    // OutputFileOptions Builder.
    //val builder: ImageCapture.OutputFileOptions.Builder

    return if (isPublicDirectory) {
        // 하위 디렉토리명.
        val childDirectory = this.packageName?.split('.')?.last() ?: "adbcamerax"
        // 공유 저장소 사용 시 Android Q 대응.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.MIME_TYPE, "image/$extension")
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.$extension")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$childDirectory"
                )
            }

            // Builder.
            ImageCapture.OutputFileOptions.Builder(
                this.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } else {
            @Suppress("DEPRECATION")
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                childDirectory
            )
            if (!directory.mkdirs()) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }

            // Builder.
            ImageCapture.OutputFileOptions.Builder(
                File("${directory.absolutePath}/$filename.$extension")
            )
        }
    } else {
        // 내부 저장소 사용.
        val directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (directory?.mkdirs() != true) {
            Timber.i(">>>>> Directory not created : %s", directory)
        }

        // Builder.
        ImageCapture.OutputFileOptions.Builder(
            File("${directory?.absolutePath}/$filename.$extension")
        )
    }
}
