@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.camera.core.impl.utils.Exif
import androidx.core.net.toFile
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.min

/**
 * Created by oooobang on 2022. 7. 20..
 * Uri Extension.
 */
// 이미지 Exif.
internal fun Uri.exif(context: Context): Exif? {
    var exif: Exif? = null
    var inputStream: InputStream? = null
    try {
        exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.openInputStream(this)?.let { stream ->
                inputStream = stream
                Exif.createFromInputStream(stream)
            }
        } else {
            Exif.createFromFile(this.toFile())
        }
        Timber.i(">>>>> exif : $exif")
    } catch (ex: Exception) {
        // Debug.
        Timber.e(">>>>> exif : $ex")
    } finally {
        inputStream?.close()
    }
    return exif
}

// 이미지 ExifInterface.
internal fun Uri.exifInterface(context: Context): ExifInterface? {
    var exifInterface: ExifInterface? = null
    var inputStream: InputStream? = null
    try {
        exifInterface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.openInputStream(this)?.let { stream ->
                inputStream = stream
                ExifInterface(stream)
            }
        } else {
            ExifInterface(this.toFile())
        }

        // Debug.
        ExifInterface::class.java.fields.forEach {
            if (it.name.startsWith("TAG_")) {
                val value = it.get(it.name) as String
                Timber.i(
                    ">>>>> exifInterface ${it.name} : " +
                            "${exifInterface?.getAttribute(value)}"
                )
            }
        }
    } catch (ex: Exception) {
        // Debug.
        Timber.e(">>>>> ExifInterface : $ex")
    } finally {
        inputStream?.close()
    }
    return exifInterface
}

// 이미지 Thumbnail 반환.
internal fun Uri.toThumbnail(
    context: Context,
    exif: Exif? = null,
    size: Int = 96
): Bitmap? {
    @Suppress("DEPRECATION")
    return if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
        val width = exif?.width ?: 0
        val height = exif?.height ?: 0
        val sample = min(width / size, height / size).let {
            if (it == 0) 1 else it
        }

        // Debug.
        Timber.i(">>>>> toThumbnail Sample : $sample")
        Timber.i(">>>>> toThumbnail origin size : ${exif?.width} x ${exif?.height}")
        Timber.i(">>>>> toThumbnail thumbnail size : ${width / sample} x ${height / sample}")

        // Thumbnail.
        context.contentResolver.loadThumbnail(
            this,
            Size(width / sample, height / sample),
            null
        )
    } else {
        val kind = when (size) {
            in 0..96 -> MediaStore.Images.Thumbnails.MICRO_KIND
            in 97..384 -> MediaStore.Images.Thumbnails.MINI_KIND
            else -> MediaStore.Images.Thumbnails.FULL_SCREEN_KIND
        }
        MediaStore.Images.Thumbnails.getThumbnail(
            context.contentResolver,
            lastPathSegment?.toLong() ?: 0,
            kind,
            null
        )
    }
}

// Bitmap 반환.
fun Uri.toBitmap(context: Context): Bitmap? {
    try {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inSampleSize = 1
        return BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(this),
            null,
            options
        )
    } catch (ex: FileNotFoundException) {
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
    val rotation = rotationDegrees?.toFloat() ?: (exif(context)?.rotation?.toFloat() ?: return null)
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
        exif(context)?.let {
            Triple(it.width, it.height, it.rotation)
        } ?: return null
    } else {
        Triple(originSize.width, originSize.height, rotationDegrees)
    }
    val matrix = Matrix().apply {
        preRotate(rotation.toFloat())
    }
    if (cropSize.width < width && cropSize.height < height) {
        val (widthCrop, heightCrop) = when (rotation) {
            90, 270 -> {
                Pair(cropSize.width, cropSize.height)
            }
            else -> {
                Pair(cropSize.height, cropSize.width)
            }
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
        exif(context)?.let {
            Triple(it.width, it.height, it.rotation)
        } ?: return null
    } else {
        Triple(originSize.width, originSize.height, rotationDegrees)
    }
    val matrix = Matrix().apply {
        preRotate(rotation.toFloat())
    }
    val (widthCrop, heightCrop) = when (rotation) {
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
        bitmap,
        (width / 2) - (widthCrop / 2),
        (height / 2) - (heightCrop / 2),
        widthCrop,
        heightCrop,
        matrix,
        true
    )
}

// 이미지 리사이징.
internal fun Uri.resize(context: Context, resize: Int): Bitmap? {
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
    } catch (ex: FileNotFoundException) {
        ex.printStackTrace()
    }
    return null
}
