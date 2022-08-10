@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.util.Size
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by oooobang on 2018. 5. 11..
 * Bitmap Extension.
 */
// 저장.
fun Bitmap?.save(
    context: Context? = null,
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    rotation: Int? = null,
    action: ((path: String?, uri: Uri?) -> Unit)? = null
): String? {
    var path: String? = null
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
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
                    Timber.i(">>>>> Q URI : %s", uri)

                    // 반환용.
                    path = uri.toString()

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
                            this?.compress(format, 95, fileOutputStream)
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
                            Timber.i(">>>>> ExifInterface Rotation : $rotation => $orientation")

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
            action?.invoke(path, path?.toUri())
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
                this?.compress(format, 95, fileOutputStream)
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }

            // Media Scanning.
            context?.mediaScanning(path) { scanPath, scanUri ->
                action?.invoke(scanPath, scanUri)
//
//                // Exif 태그 데이터를 이미지 파일에 저장.
//                scanUri?.exif(context)?.also { exif ->
//                    val orientation = when (rotation) {
//                        0 -> ExifInterface.ORIENTATION_NORMAL
//                        90 -> ExifInterface.ORIENTATION_ROTATE_90
//                        180 -> ExifInterface.ORIENTATION_ROTATE_180
//                        270 -> ExifInterface.ORIENTATION_ROTATE_270
//                        else -> null
//                    }
//                    orientation?.let {
//                        exif.rotate(it)
//                        exif.save()
//                    }
//                }
            }
        }
    } else {
        /* 내부 저장소 사용. */
        var fileOutputStream: FileOutputStream? = null
        try {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (directory?.mkdirs() != true) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }
            path = "${directory?.absolutePath}/$filename.$extension"
            fileOutputStream = FileOutputStream(path)
            this?.compress(format, 95, fileOutputStream)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }
    }

    // Debug.
    Timber.i(">>>>> Save Bitmap Finish : %s", path)

    return path
}

// toByteArray.
fun Bitmap?.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): ByteArray? {
    val stream = ByteArrayOutputStream()
    this?.compress(format, 95, stream)
    return stream.toByteArray()
}

// 특정 컬러 투명처리.
fun Bitmap?.toTransparentBitmap(replaceThisColor: Int): Bitmap? {
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

// 이미지 Uri에서 회전후 중앙 기준 Crop한 Bitmap 반환.
fun Bitmap.rotateAndCenterCrop(
    cropSize: Size,
    rotationDegrees: Int
): Bitmap? {
    val matrix = Matrix().apply {
        preRotate(rotationDegrees.toFloat())
    }
    if (cropSize.width < width && cropSize.height < height) {
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> {
                Pair(cropSize.width, cropSize.height)
            }
            else -> {
                Pair(cropSize.height, cropSize.width)
            }
        }
        return Bitmap.createBitmap(
            this,
            (width / 2) - (widthCrop / 2),
            (height / 2) - (heightCrop / 2),
            widthCrop,
            heightCrop,
            matrix,
            true
        )
    } else {
        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            true
        )
    }
}

// 이미지 Uri에서 회전후 중앙 기준 Crop한 Bitmap 반환.
fun Bitmap.rotateAndCenterCrop(
    cropPercent: Array<Float>,
    rotationDegrees: Int
): Bitmap? {
    val matrix = Matrix().apply {
        preRotate(rotationDegrees.toFloat())
    }
    val (widthCrop, heightCrop) = when (rotationDegrees) {
        90, 270 -> {
            Pair(
                (width * cropPercent[1]).toInt(),
                (height * cropPercent[0]).toInt()
            )
        }
        else -> {
            Pair(
                (width * cropPercent[0]).toInt(),
                (height * cropPercent[1]).toInt()
            )
        }
    }
    return Bitmap.createBitmap(
        this,
        (width / 2) - (widthCrop / 2),
        (height / 2) - (heightCrop / 2),
        widthCrop,
        heightCrop,
        matrix,
        true
    )
}

// 리사이징.
fun Bitmap?.resize(resizePixcel: Int): Bitmap? {
    try {
        return this?.let { bitmap ->
            val sample = if (width >= height) {
                resizePixcel.toFloat() / width.toFloat()
            } else {
                resizePixcel.toFloat() / height.toFloat()
            }
            val (sampleWidth, sampleHeight) = if (sample < 1) {
                Pair((width.toFloat() * sample).toInt(), (height.toFloat() * sample).toInt())
            } else {
                Pair(width, height)
            }
            Bitmap.createScaledBitmap(bitmap, sampleWidth, sampleHeight, true)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}

// 리사이징.
fun Bitmap?.optimumResize (resize: Int): Bitmap? {
    try {
        return this?.let { bitmap ->
//            val byteArray = bitmap.toByteArray()
//            val options = BitmapFactory.Options().apply {
//                inJustDecodeBounds = true
//            }
//            BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size ?: 0, options)
//            var width = options.outWidth
//            var height = options.outHeight
//            var sampleSize = 1
            while (true) {
                if (width / 2 < resize || height / 2 < resize) {
                    break
                }
                width /= 2
                height /= 2
//                sampleSize *= 2
            }
//            options.inSampleSize = sampleSize
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
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
