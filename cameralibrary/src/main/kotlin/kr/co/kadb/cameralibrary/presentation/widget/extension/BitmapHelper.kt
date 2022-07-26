@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

/**
 * Created by oooobang on 2018. 5. 11..
 * Bitmap Extension.
 */
internal fun Bitmap?.save(
    context: Context? = null,
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): String? {
    // Debug.
    Timber.i(">>>>> Save ByteArray")

    var path: String? = null
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
        else -> "webp"
    }

    if (isPublicDirectory) {
        // 하위 디렉토리명.
        val childDirectory = context?.packageName?.split('.')?.last() ?: "adbcamerax"
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

            context?.contentResolver?.let { contentResolver ->
                // ContentResolver을 통해 insert.
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                contentResolver.insert(collection, contentValues)?.let { uri ->
                    // Debug.
                    Timber.i(">>>>> Q URI : %s", uri)

                    // 반환용.
                    path = uri.toString()

                    // 파일 쓰.
                    var fileOutputStream: FileOutputStream? = null
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        // Uri(item)의 위치에 파일을 생성해준다.
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            uri,
                            "w",
                            null
                        )
                        parcelFileDescriptor?.let {
                            fileOutputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
                            this?.compress(format, 90, fileOutputStream)
                            contentResolver.update(uri, contentValues, null, null)
                        }
                    } catch (ex: Exception) {
                        // Debug.
                        Timber.e(">>>>> Save ByteArray Exception : %s", ex.toString())
                    } finally {
                        fileOutputStream?.close()
                        parcelFileDescriptor?.close()
                    }
                    contentValues.clear()

                    // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
            }
        } else {
            var fileOutputStream: FileOutputStream? = null
            try {
                @Suppress("DEPRECATION")
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    childDirectory
                )
                if (!directory.mkdirs()) {
                    Timber.i(">>>>> Directory not created : %s", directory)
                }

                path = "${directory.absolutePath}/$filename.$extension"
                fileOutputStream = FileOutputStream(path)
                this?.compress(format, 90, fileOutputStream)

                // Media Scanning.
                path?.let {
                    context?.mediaScanning(it)
                }
            } catch (ex: Exception) {
                // Debug.
                Timber.e(">>>>> Save ByteArray Exception : %s", ex.toString())
            } finally {
                fileOutputStream?.close()
            }
        }
    } else {
        // 내부 저장소 사용.
        var fileOutputStream: FileOutputStream? = null
        try {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (directory?.mkdirs() != true) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }
            path = "${directory?.absolutePath}/$filename.$extension"
            fileOutputStream = FileOutputStream(path)
            this?.compress(format, 90, fileOutputStream)
        } catch (ex: Exception) {
            // Debug.
            Timber.e(">>>>> Save Bitmap Exception : %s", ex.toString())
        } finally {
            fileOutputStream?.close()
        }
    }

    // Debug.
    Timber.i(">>>>> Save Bitmap Finish : %s", path)

    return path
}

// toByteArray.
internal fun Bitmap?.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): ByteArray? {
    val stream = ByteArrayOutputStream()
    this?.compress(format, 90, stream)
    return stream.toByteArray()
}

/**
 * Bitmap 특정 컬러 투명처리.
 */
internal fun Bitmap?.toTransparentBitmap(replaceThisColor: Int): Bitmap? {
    if (this != null) {
        val picw = this.width
        val pich = this.height
        val pix = IntArray(picw * pich)
        this.getPixels(pix, 0, picw, 0, 0, picw, pich)
        for (y in 0 until pich) {
            for (x in 0 until picw) {
                val index = y * picw + x
                if (pix[index] == replaceThisColor) {
                    pix[index] = Color.TRANSPARENT
                }
            }
            for (x in picw - 1 downTo 0) {
                val index = y * picw + x
                if (pix[index] == replaceThisColor) {
                    pix[index] = Color.TRANSPARENT
                }
            }
        }
        return Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888)
    }
    return null
}

// Base64.
fun Bitmap?.toBase64(
    flags: Int = Base64.NO_WRAP,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): String? {
    return Base64.encodeToString(toByteArray(format), flags)
}
