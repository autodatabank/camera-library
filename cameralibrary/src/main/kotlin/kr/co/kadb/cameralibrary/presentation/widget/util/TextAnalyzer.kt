package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData

/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class TextAnalyzer(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val result: MutableLiveData<String>,
    private val imageCropPercentages: MutableLiveData<Pair<Int, Int>>,
    private var action: ((Bitmap) -> Unit)
) : ImageAnalysis.Analyzer {

    // TODO: Instantiate TextRecognition detector

    // TODO: Add lifecycle observer to properly close ML Kit detectors

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // We requested a setTargetAspectRatio, but it's not guaranteed that's what the camera
        // stack is able to support, so we calculate the actual ratio from the first frame to
        // know how to appropriately crop the image we want to analyze.
        val imageHeight = mediaImage.height
        val imageWidth = mediaImage.width

        val actualAspectRatio = imageWidth / imageHeight

        val convertImageToBitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)
        val cropRect = Rect(0, 0, imageWidth, imageHeight)

        // If the image has a way wider aspect ratio than expected, crop less of the height so we
        // don't end up cropping too much of the image. If the image has a way taller aspect ratio
        // than expected, we don't have to make any changes to our cropping so we don't handle it
        // here.
        val currentCropPercentages = imageCropPercentages.value ?: return
        if (actualAspectRatio > 3) {
            val originalHeightCropPercentage = currentCropPercentages.first
            val originalWidthCropPercentage = currentCropPercentages.second
            imageCropPercentages.value =
                Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
        }

        // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
        // the crop.
        val cropPercentages = imageCropPercentages.value ?: return
        val heightCropPercent = cropPercentages.first
        val widthCropPercent = cropPercentages.second
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
            else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
        }

        cropRect.inset(
            (imageWidth * widthCrop / 2).toInt(),
            (imageHeight * heightCrop / 2).toInt()
        )
        val croppedBitmap = ImageUtils.rotateAndCrop(convertImageToBitmap, rotationDegrees, cropRect)

        // TODO call recognizeText() once implemented
        action.invoke(croppedBitmap)
    }

    fun recognizeText() {
        // TODO Use ML Kit's TextRecognition to analyze frames from the camera live feed.
    }
//
//    private fun getErrorMessage(exception: Exception): String? {
//        val mlKitException = exception as? MlKitException ?: return exception.message
//        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
//            "Waiting for text recognition model to be downloaded"
//        } else exception.message
//    }

    companion object {
        private const val TAG = "TextAnalyzer"
    }
}