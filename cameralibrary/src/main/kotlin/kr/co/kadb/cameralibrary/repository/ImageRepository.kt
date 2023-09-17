package kr.co.kadb.cameralibrary.repository

import android.net.Uri
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kr.co.kadb.cameralibrary.CameraLibraryInitProvider
import kr.co.kadb.cameralibrary.presentation.model.Image
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.widget.extension.centerCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
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
            cropedBitmap.save(
                CameraLibraryInitProvider().requireContext,
                true,
                exifInterface = exifInterface,
                jpegQuality = croppedJpegQuality
            ) { imagePath, imageUri ->
                trySend(
                    Image(
                        path = imagePath ?: "",
                        uri = imageUri ?: Uri.EMPTY,
                        size = Size(cropedBitmap?.width ?: 0, cropedBitmap?.height ?: 0)
                    )
                )
            }
            cropedBitmap?.recycle()
        } else {
            // Save Bitmap.
            byteArray.save(
                CameraLibraryInitProvider().requireContext, true
            ) { imagePath, imageUri ->
                trySend(
                    Image(
                        path = imagePath ?: "",
                        uri = imageUri ?: Uri.EMPTY
                    )
                )
            }
        }
        awaitClose()
    }
}
