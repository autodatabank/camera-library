@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        Timber.i(">>>>> Exif : $exif")
    } catch (ex: Exception) {
        // Debug.
        Timber.e(">>>>> Exif : $ex")
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
                    ">>>>> ExifInterface ${it.name} : " +
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
internal fun Uri.thumbnail(
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
        Timber.i(">>>>> Thumbnail Sample : $sample")
        Timber.i(">>>>> Thumbnail Origin Size : ${exif?.width} x ${exif?.height}")
        Timber.i(">>>>> Thumbnail Sample Size : ${width / sample} x ${height / sample}")

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
