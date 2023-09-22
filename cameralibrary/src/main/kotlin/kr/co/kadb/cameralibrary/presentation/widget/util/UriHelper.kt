package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.util.Size
import androidx.annotation.FloatRange
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBitmap
import kr.co.kadb.cameralibrary.presentation.widget.extension.toThumbnail

/**
 * Utility class for Uri.
 */
public class UriHelper {
    public companion object {
        @JvmStatic
        public fun toBitmap(context: Context, uri: Uri?): Bitmap? {
            return uri?.toBitmap(context)
        }

        @JvmStatic
        public fun toThumbnail(context: Context, uri: Uri?): Bitmap? {
            return uri?.toThumbnail(context)
        }

        @JvmStatic
        public fun toThumbnail(context: Context, uri: Uri?, originSize: Size?): Bitmap? {
            return uri?.toThumbnail(context, originSize)
        }

        @JvmStatic
        public fun toThumbnail(
            context: Context,
            uri: Uri?,
            originSize: Size?,
            thumbnailSize: Int = 96
        ): Bitmap? {
            return uri?.toThumbnail(context, originSize, thumbnailSize)
        }

        @JvmStatic
        public fun rotateAndCrop(context: Context, uri: Uri?, cropRect: Rect): Bitmap? {
            return uri?.rotateAndCrop(context, cropRect)
        }

        @JvmStatic
        public fun rotateAndCrop(
            context: Context,
            uri: Uri?,
            cropRect: Rect,
            rotationDegrees: Int?
        ): Bitmap? {
            return uri?.rotateAndCrop(context, cropRect, rotationDegrees)
        }

        @Deprecated(
            message = "직관성을 위하여 Deprecated.",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith(
                "rotateAndCenterCrop(Context, Uri, Array<Float>)",
                "rotateAndCenterCrop(Context, Uri, Float, Float)"
            )
        )
        @JvmStatic
        public fun rotateAndCenterCrop(
            context: Context,
            uri: Uri?,
            cropPercent: Array<Float>
        ): Bitmap? {
            return uri?.rotateAndCenterCrop(context, cropPercent[0], cropPercent[1])
        }

        @Deprecated(
            message = "직관성을 위하여 Deprecated.",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith(
                "rotateAndCenterCrop(Context, Uri, Array<Float>, Int)",
                "rotateAndCenterCrop(Context, Uri, Float, Float, Int)"
            )
        )
        @JvmStatic
        public fun rotateAndCenterCrop(
            context: Context,
            uri: Uri?,
            cropPercent: Array<Float>,
            rotationDegrees: Int?
        ): Bitmap? {
            return uri?.rotateAndCenterCrop(
                context,
                cropPercent[0],
                cropPercent[1],
                rotationDegrees = rotationDegrees
            )
        }

        @JvmStatic
        public fun rotateAndCenterCrop(
            context: Context,
            uri: Uri?,
            @FloatRange(from = 0.0, to = 1.0)
            cropWidth: Float,
            @FloatRange(from = 0.0, to = 1.0)
            cropHeight: Float
        ): Bitmap? {
            return uri?.rotateAndCenterCrop(context, cropWidth, cropHeight)
        }

        @JvmStatic
        public fun rotateAndCenterCrop(
            context: Context,
            uri: Uri?,
            @FloatRange(from = 0.0, to = 1.0)
            cropWidth: Float,
            @FloatRange(from = 0.0, to = 1.0)
            cropHeight: Float,
            rotationDegrees: Int?
        ): Bitmap? {
            return uri?.rotateAndCenterCrop(
                context = context,
                cropWidth = cropWidth,
                cropHeight = cropHeight,
                rotationDegrees = rotationDegrees
            )
        }
    }
}
