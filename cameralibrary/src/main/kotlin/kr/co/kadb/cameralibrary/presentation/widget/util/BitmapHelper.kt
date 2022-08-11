package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
<<<<<<< HEAD
=======
import kr.co.kadb.cameralibrary.presentation.widget.extension.optimumResize
>>>>>>> origin/develop
import kr.co.kadb.cameralibrary.presentation.widget.extension.resize
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBase64

/**
 * Utility class for Bitmap.
 */
class BitmapHelper {
    companion object {
        @JvmStatic
        fun save(
            context: Context,
            bitmap: Bitmap?,
            isPublicDirectory: Boolean = false
        ): String? {
            return bitmap?.save(context, isPublicDirectory)
        }

        @JvmStatic
        fun save(
            context: Context,
            bitmap: Bitmap?,
            isPublicDirectory: Boolean = false,
            rotation: Int?
        ): String? {
            return bitmap?.save(
                context = context,
                isPublicDirectory = isPublicDirectory,
                rotation = rotation
            )
        }

        @JvmStatic
        fun save(
            context: Context,
            bitmap: Bitmap?,
            isPublicDirectory: Boolean = false,
            filename: String = System.currentTimeMillis().toString(),
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): String? {
            return bitmap?.save(
                context = context,
                isPublicDirectory = isPublicDirectory,
                filename = filename,
                format = format
            )
        }

        @JvmStatic
        fun save(
            context: Context,
            bitmap: Bitmap?,
            isPublicDirectory: Boolean = false,
            filename: String = System.currentTimeMillis().toString(),
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
            rotation: Int?
        ): String? {
            return bitmap?.save(
                context = context,
                isPublicDirectory = isPublicDirectory,
                filename = filename,
                format = format,
                rotation = rotation
            )
        }

        @JvmStatic
        fun resize(bitmap: Bitmap?, resizePixcel: Int): Bitmap? {
            return bitmap?.resize(resizePixcel)
        }

        @JvmStatic
        fun optimumResize(bitmap: Bitmap?, resizePixcel: Int): Bitmap? {
            return bitmap?.optimumResize(resizePixcel)
        }

        @JvmStatic
        fun resize(bitmap: Bitmap?, resizePixcel: Int): Bitmap? {
            return bitmap?.resize(resizePixcel)
        }

        @JvmStatic
        fun toBase64(
            bitmap: Bitmap?
        ): String? {
            return bitmap.toBase64()
        }

        @JvmStatic
        fun toBase64(
            bitmap: Bitmap?,
            flags: Int = Base64.NO_WRAP,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): String? {
            return bitmap.toBase64()
        }
    }
}
