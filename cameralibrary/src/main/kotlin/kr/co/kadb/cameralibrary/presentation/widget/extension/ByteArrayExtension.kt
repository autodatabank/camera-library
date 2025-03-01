package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog
import java.io.File
import java.io.FileOutputStream

/**
 * ByteArray Extension.
 */
// 저장.
internal fun ByteArray?.saveImage(
    context: Context? = null,
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    rotation: Int? = null,
    action: ((savedPath: String?) -> Unit)? = null
): String? {
    var imagePath: String? = null
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpeg"
        else -> "webp"
    }

    if (isPublicDirectory) {
        /* 공용 저장소 사용. */
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

            // 저장.
            context?.contentResolver?.let { contentResolver ->
                // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
                // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                contentResolver.insert(collection, contentValues)?.let { uri ->
                    // Debug.
                    DebugLog.i { ">>>>> Q URI : $uri" }

                    // 반환용.
                    imagePath = uri.toString()

                    // 파일 쓰기.
                    var fileOutputStream: FileOutputStream? = null
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        // Uri(item)의 위치에 파일을 생성해준다.
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            uri, "w", null
                        )
                        parcelFileDescriptor?.fileDescriptor?.also { fileDescriptor ->
                            fileOutputStream = FileOutputStream(fileDescriptor)
                            fileOutputStream?.write(this)
                            contentResolver.update(uri, contentValues, null, null)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
                        fileOutputStream?.close()
                        parcelFileDescriptor?.close()
                    }
                    contentValues.clear()

                    // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)

                    // Exif 태그 데이터를 이미지 파일에 저장.
                    try {
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            uri, "rw", null
                        )
                        parcelFileDescriptor?.fileDescriptor?.also { fileDescriptor ->
                            val orientation = when (rotation) {
                                0 -> ExifInterface.ORIENTATION_NORMAL
                                90 -> ExifInterface.ORIENTATION_ROTATE_90
                                180 -> ExifInterface.ORIENTATION_ROTATE_180
                                270 -> ExifInterface.ORIENTATION_ROTATE_270
                                else -> null
                            }
                            // Debug.
                            DebugLog.i { ">>>>> ExifInterface Rotation : $rotation => $orientation" }

                            orientation?.let {
                                val exifInterface = ExifInterface(fileDescriptor)
                                exifInterface.setAttribute(
                                    ExifInterface.TAG_ORIENTATION, it.toString()
                                )
                                exifInterface.saveAttributes()
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
                        parcelFileDescriptor?.close()
                    }
                }
            }
            action?.invoke(imagePath)
        } else {
            var fileOutputStream: FileOutputStream? = null
            try {
                @Suppress("DEPRECATION")
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    childDirectory
                )
                if (!directory.mkdirs()) {
                    DebugLog.i { ">>>>> Directory not created : $directory" }
                }

                val imageFile = File("${directory.absolutePath}/$filename.$extension")
                fileOutputStream = FileOutputStream(imageFile)
                fileOutputStream?.write(this)

                // Exif 태그 데이터를 이미지 파일에 저장.
                val orientation = when (rotation) {
                    0 -> ExifInterface.ORIENTATION_NORMAL
                    90 -> ExifInterface.ORIENTATION_ROTATE_90
                    180 -> ExifInterface.ORIENTATION_ROTATE_180
                    270 -> ExifInterface.ORIENTATION_ROTATE_270
                    else -> null
                }

                orientation?.let {
                    val exifInterface = ExifInterface(imageFile)
                    exifInterface.setAttribute(
                        ExifInterface.TAG_ORIENTATION, it.toString()
                    )
                    exifInterface.saveAttributes()
                }

                // 반환용.
                imagePath = imageFile.absolutePath
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }

            // Media Scanning.
            context?.mediaScanning(imagePath) { scanPath, scanUri ->
                action?.invoke(scanUri?.toString() ?: scanPath)
            }
        }
    } else {
        /* 내부 저장소 사용. */
        var fileOutputStream: FileOutputStream? = null
        try {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (directory?.mkdirs() != true) {
                DebugLog.i { ">>>>> Directory not created : $directory" }
            }

            val imageFile = File("${directory?.absolutePath}/$filename.$extension")
            fileOutputStream = FileOutputStream(imageFile)
            fileOutputStream?.write(this)

            // 반환용.
            imagePath = imageFile.absolutePath
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }
        action?.invoke(imagePath)
    }

    // Debug.
    DebugLog.i { ">>>>> Save ByteArray Finish : $imagePath" }

    return imagePath
}

// To Bitmap.
internal fun ByteArray?.toBitmap(sampleSize: Int = 1): Bitmap? {
    // 샘플링.
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    return BitmapFactory.decodeByteArray(this, 0, this?.size ?: 0, options)
}
