package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.min

/**
 * Created by oooobang on 2022. 7. 20..
 * Uri Extension.
 */
/*// 이미지 Exif.
fun Uri.exif(context: Context): Exif? {
    var exif: Exif? = null
    var inputStream: InputStream? = null
    try {
        exif = context.contentResolver.openInputStream(this)?.let { stream ->
            inputStream = stream
            Exif.createFromInputStream(stream)
        }
        Timber.i(">>>>> exif : $exif")
    } catch (ex: Exception) {
        ex.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return exif
}*/

// 이미지 ExifInterface.
fun Uri.exifInterface(context: Context): ExifInterface? {
    var exifInterface: ExifInterface? = null
    var inputStream: InputStream? = null
    try {
        exifInterface = context.contentResolver.openInputStream(this)?.let { stream ->
            inputStream = stream
            ExifInterface(stream)
        }
//        // Debug.
//        if (BuildConfig.DEBUG) {
//            ExifInterface::class.java.fields.forEach {
//                if (it.name.startsWith("TAG_")) {
//                    val value = it.get(it.name) as String
//                    Timber.i(
//                        ">>>>> exifInterface ${it.name} : " +
//                                "${exifInterface?.getAttribute(value)}"
//                    )
//                }
//            }
//        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return exifInterface
}

// 이미지 Thumbnail 반환.
fun Uri.toThumbnail(
    context: Context,
    originSize: Size? = null,
    thumbnailSize: Int = 96
): Bitmap? {
    @Suppress("DEPRECATION")
    return if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
        val (width, height) = if (originSize == null) {
            val exifInterface = this.exifInterface(context)
            Pair(
                exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0) ?: 0,
                exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0) ?: 0
            )
        } else {
            Pair(originSize.width, originSize.height)
        }
        val sample = min(width / thumbnailSize, height / thumbnailSize).let {
            if (it == 0) 1 else it
        }
        // Thumbnail.
        context.contentResolver.loadThumbnail(
            this, Size(width / sample, height / sample), null
        )
    } else {
        val kind = when (thumbnailSize) {
            in 0..96 -> MediaStore.Images.Thumbnails.MICRO_KIND
            in 97..384 -> MediaStore.Images.Thumbnails.MINI_KIND
            else -> MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
        }
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            MediaStore.MediaColumns.DATA + "=?",
            arrayOf(this.toString()),
            null
        )
        if (cursor?.moveToFirst() == true) {
            @SuppressLint("Range")
            val imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            return MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver,
                imageId.toLong(),
                kind,
                null
            )
        }
        cursor?.close()
        return null
    }
}

// Bitmap 반환.
@Suppress("DEPRECATION")
fun Uri.toBitmap(
    context: Context
): Bitmap? {
    try {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}

// 이미지 Uri에서 회전후 Crop한 Bitmap 반환.
fun Uri.rotateAndCrop(
    context: Context,
    cropRect: Rect,
    rotationDegrees: Int? = null
): Bitmap? {
    val rotation = rotationDegrees?.toFloat() ?: (exifInterface(context)?.rotationDegrees?.toFloat()
        ?: return null)
    val bitmap = toBitmap(context) ?: return null
    val matrix = Matrix()
    matrix.preRotate(rotation)
    return Bitmap.createBitmap(
        bitmap,
        cropRect.left,
        cropRect.top,
        cropRect.width(),
        cropRect.height(),
        matrix,
        true
    )
}

// 이미지 Uri에서 회전후 중앙 기준 Crop한 Bitmap 반환.
fun Uri.rotateAndCenterCrop(
    context: Context,
    cropSize: Size,
    originSize: Size? = null,
    rotationDegrees: Int? = null
): Bitmap? {
    val bitmap = toBitmap(context) ?: return null
    val (width, height, rotation) = if (originSize == null || rotationDegrees == null) {
        exifInterface(context)?.let {
            Triple(
                it.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0),
                it.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0),
                it.rotationDegrees
            )
        } ?: return null
    } else {
        Triple(originSize.width, originSize.height, rotationDegrees)
    }
    val matrix = rotationDegrees?.let {
        Matrix()
    }?.also {
        it.preRotate(rotation.toFloat())
    }
    if (cropSize.width < width && cropSize.height < height) {
        val (widthCrop, heightCrop) = when (rotation) {
            90, 270 -> Pair(cropSize.height, cropSize.width)
            else -> Pair(cropSize.width, cropSize.height)
        }
        return Bitmap.createBitmap(
            bitmap,
            (width / 2) - (widthCrop / 2),
            (height / 2) - (heightCrop / 2),
            widthCrop,
            heightCrop,
            matrix,
            true
        )
    } else {
        return Bitmap.createBitmap(
            bitmap,
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
fun Uri.rotateAndCenterCrop(
    context: Context,
    cropPercent: Array<Float>,
    originSize: Size? = null,
    rotationDegrees: Int? = null
): Bitmap? {
    val bitmap = toBitmap(context) ?: return null
    val (width, height, rotation) = if (originSize == null || rotationDegrees == null) {
        exifInterface(context)?.let {
            Triple(
                it.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0),
                it.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0),
                it.rotationDegrees
            )
        } ?: return null
    } else {
        Triple(originSize.width, originSize.height, rotationDegrees)
    }
    val matrix = rotationDegrees?.let {
        Matrix()
    }?.also {
        it.preRotate(rotation.toFloat())
    }
    val (cropRect, cropSize) = when (rotation) {
        90, 270 -> {
            val widthCrop = (height * cropPercent[0]).toInt()
            val heightCrop = (width * cropPercent[1]).toInt()
            val x = (height / 2) - (widthCrop / 2)
            val y = (width / 2) - (heightCrop / 2)
            Pair(
                Rect(x, y, x + widthCrop, y + heightCrop),
                Size(widthCrop, heightCrop)
            )
        }
        else -> {
            val widthCrop = (width * cropPercent[0]).toInt()
            val heightCrop = (height * cropPercent[1]).toInt()
            val x = (width / 2) - (widthCrop / 2)
            val y = (height / 2) - (heightCrop / 2)
            Pair(
                Rect(x, y, x + widthCrop, y + heightCrop),
                Size(widthCrop, heightCrop)
            )
        }
    }

    return Bitmap.createBitmap(
        bitmap,
        cropRect.left,
        cropRect.top,
        cropSize.width,
        cropSize.height,
        matrix,
        true
    )
}

// 이미지 리사이징.
fun Uri.resize(context: Context, resize: Int): Bitmap? {
    try {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(this), null, options)
        var width = options.outWidth
        var height = options.outHeight
        var sampleSize = 1
        while (true) {
            if (width / 2 < resize || height / 2 < resize) {
                break
            }
            width /= 2
            height /= 2
            sampleSize *= 2
        }
        options.inSampleSize = sampleSize
        return BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(this),
            null,
            options
        )
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}
