package kr.co.kadb.cameralibrary.repository

import android.net.Uri
import android.util.Size
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kr.co.kadb.cameralibrary.CameraLibraryInitProvider
import kr.co.kadb.cameralibrary.presentation.model.Image
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.widget.extension.*
import kr.co.kadb.cameralibrary.presentation.widget.extension.centerCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBitmap
import java.io.ByteArrayInputStream
import java.io.IOException

internal class ImageRepository {

    // 이미지 저장.
    fun save(
        byteArray: ByteArray,
        cropSize: CropSize,
        croppedJpegQuality: Int
    ): Flow<Image> = callbackFlow {

        if (cropSize.isNotEmpty) {

            // Get the original ExifInterface.
            var exifInterface: ExifInterface? = null
            try {
                ByteArrayInputStream(byteArray).use { byteArrayInputStream ->
                    exifInterface = ExifInterface(byteArrayInputStream)
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            // Crop.
            val bitmap = byteArray.toBitmap()
            val cropedBitmap = bitmap?.centerCrop(
                cropSize.width,
                cropSize.height,
                exifInterface?.rotationDegrees
            )
            bitmap?.recycle()

            // Save Bitmap.
            cropedBitmap.saveImage(
                CameraLibraryInitProvider().requireContext,
                true,
                exifInterface = exifInterface,
                jpegQuality = croppedJpegQuality
            ) { savedPath, originBitmap ->
                originBitmap?.recycle()
                trySend(
                    Image(
                        path = savedPath ?: "",
                        uri = savedPath?.toUri() ?: Uri.EMPTY,
                        size = Size(cropedBitmap?.width ?: 0, cropedBitmap?.height ?: 0)
                    )
                )
            }
            cropedBitmap?.recycle()
        } else {
            // Save Bitmap.
            byteArray.saveImage(
                CameraLibraryInitProvider().requireContext, true
            ) { savedPath ->
                trySend(
                    Image(
                        path = savedPath ?: "",
                        uri = savedPath?.toUri() ?: Uri.EMPTY
                    )
                )
            }
        }
        awaitClose()
    }
}
