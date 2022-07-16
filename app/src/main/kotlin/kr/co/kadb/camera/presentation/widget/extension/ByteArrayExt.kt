@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Created by oooobang on 2018. 5. 11..
 * ByteArray Extension.
 */
internal fun ByteArray?.save(
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.$extension")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/signing")
            }

            context?.contentResolver?.let { contentResolver ->
                // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
                // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                contentResolver.insert(collection, contentValues)?.let { uri ->
                    // Debug.
                    Timber.i(">>>>> Q URI : %s", uri)

                    //
                    path = uri.toString()

                    //
                    var fileOutputStream: FileOutputStream? = null
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        // Uri(item)의 위치에 파일을 생성해준다.
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                                uri,
                                "w",
                                null)
                        parcelFileDescriptor?.let {
                            fileOutputStream = FileOutputStream(parcelFileDescriptor.fileDescriptor)
                            fileOutputStream?.write(this)

                            //
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
                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "signing")
                if (!directory.mkdirs()) {
                    Timber.i(">>>>> Directory not created : %s", directory)
                }

                path = "${directory.absolutePath}/$filename.$extension"
                fileOutputStream = FileOutputStream(path)
                fileOutputStream?.write(this)

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
        var fileOutputStream: FileOutputStream? = null
        try {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (directory?.mkdirs() != true) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }
            path = "${directory?.absolutePath}/$filename.$extension"
            fileOutputStream = FileOutputStream(path)
            fileOutputStream?.write(this)
        } catch (ex: Exception) {
            // Debug.
            Timber.e(">>>>> Save ByteArray Exception : %s", ex.toString())
        } finally {
            fileOutputStream?.close()
        }
    }

    // Debug.
    Timber.i(">>>>> Save ByteArray Finish : %s", path)

    return path
}

// toByteArray.
internal fun ByteArray?.toBitmap(sampleSize: Int? = null): Bitmap? {
    // 샘플링.
    val options = BitmapFactory.Options()
    sampleSize?.let {
        options.inSampleSize = it
    }
    return BitmapFactory.decodeByteArray(this, 0, this?.size ?: 0, options)
}
