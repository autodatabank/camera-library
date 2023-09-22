package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import kr.co.kadb.cameralibrary.presentation.widget.extension.optimumResize
import kr.co.kadb.cameralibrary.presentation.widget.extension.resize
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBase64

/**
 * Utility class for Bitmap.
 */
public class BitmapHelper {
    public companion object {
        @JvmStatic
        public fun save(
            context: Context,
            bitmap: Bitmap?,
            isPublicDirectory: Boolean = false
        ): String? {
            return bitmap?.save(context, isPublicDirectory)
        }

        @JvmStatic
        public fun save(
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
        public fun resize(bitmap: Bitmap?, resizePixcel: Int): Bitmap? {
            return bitmap?.resize(resizePixcel)
        }

        @JvmStatic
        public fun optimumResize(bitmap: Bitmap?, resizePixcel: Int): Bitmap? {
            return bitmap?.optimumResize(resizePixcel)
        }

        @JvmStatic
        public fun toBase64(
            bitmap: Bitmap?
        ): String? {
            return bitmap.toBase64()
        }

        @JvmStatic
        public fun toBase64(
            bitmap: Bitmap?,
            flags: Int = Base64.NO_WRAP,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): String? {
            return bitmap.toBase64()
        }
    }
}
