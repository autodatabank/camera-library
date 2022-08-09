package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.toThumbnail

/**
 * Utility class for Uri.
 */
class UriHelper {
    companion object {
        @JvmStatic
        fun toThumbnail(
            context: Context,
            uri: Uri?
        ): Bitmap? {
            return uri?.toThumbnail(context)
        }

        @JvmStatic
        fun toThumbnail(
            context: Context,
            uri: Uri?,
            originSize: Size?
        ): Bitmap? {
            return uri?.toThumbnail(context, originSize)
        }

        @JvmStatic
        fun toThumbnail(
            context: Context,
            uri: Uri?,
            originSize: Size?,
            thumbnailSize: Int = 96
        ): Bitmap? {
            return uri?.toThumbnail(context, originSize)
        }

        @JvmStatic
        fun rotateAndCenterCrop(
            context: Context,
            uri: Uri?,
            cropPercent: Array<Float>
        ): Bitmap? {
            return uri?.rotateAndCenterCrop(context, cropPercent)
        }
    }
}
