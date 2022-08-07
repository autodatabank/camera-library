package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
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
            isPublicDirectory: Boolean = false,
            filename: String = System.currentTimeMillis().toString(),
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): String? {
            return bitmap?.save(context, isPublicDirectory, filename, format)
        }

        @JvmStatic
        fun base64(
            bitmap: Bitmap?
        ): String? {
            return bitmap.toBase64()
        }

        @JvmStatic
        fun base64(
            bitmap: Bitmap?,
            flags: Int = Base64.NO_WRAP,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): String? {
            return bitmap.toBase64()
        }
    }
}
