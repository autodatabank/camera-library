package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop

/**
 * Utility class for Uri.
 */
class UriHelper {
    companion object {
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
