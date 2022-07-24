package kr.co.kadb.cameralibrary.presentation.widget.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import kr.co.kadb.cameralibrary.ml.LiteModelRosettaDr1
import org.tensorflow.lite.DataType
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber

private class ImageAnalyzer(
    context: Context,
    private val imageCropPercentages: MutableLiveData<Pair<Int, Int>>
) :
    ImageAnalysis.Analyzer {

    // TODO 1: Add class variable TensorFlow Lite Model
    // Initializing the flowerModel by lazy so that it runs in the same thread when the process
    // method is called.
    private val model: LiteModelRosettaDr1 by lazy {

        // TODO 6. Optional GPU acceleration
        val compatList = CompatibilityList()

        val options = if (compatList.isDelegateSupportedOnThisDevice) {
            // Debug.
            Timber.d(">>>>> ImageAnalyzer : This device is GPU Compatible")
            Model.Options.Builder().setDevice(Model.Device.GPU).build()
        } else {
            // Debug.
            Timber.d(">>>>> ImageAnalyzer : This device is GPU Incompatible")
            Model.Options.Builder().setNumThreads(4).build()
        }

        // Initialize the Flower Model
        LiteModelRosettaDr1.newInstance(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val imageWidth = image.width
        val imageHeight = image.height
        val actualAspectRatio = imageWidth / imageHeight
//
//            //Debug.
//            Timber.i(">>>>> ImageAnalyzer rotationDegrees : ${rotationDegrees}")
//            Timber.i(">>>>> ImageAnalyzer imageWidth : ${imageWidth}")
//            Timber.i(">>>>> ImageAnalyzer imageHeight : ${imageHeight}")
//            Timber.i(">>>>> ImageAnalyzer actualAspectRatio : ${actualAspectRatio}")
//            Timber.i(">>>>> ImageAnalyzer imageCropPercentages : ${imageCropPercentages.value}")

        // TODO 2: Convert Image to Bitmap then to TensorImage
        val convertImageToBitmap = ImageUtils.convertYuv420888ImageToBitmap(image)
        val cropRect = Rect(0, 0, imageWidth, imageHeight)

        // If the image has a way wider aspect ratio than expected, crop less of the height so we
        // don't end up cropping too much of the image. If the image has a way taller aspect ratio
        // than expected, we don't have to make any changes to our cropping so we don't handle it
        // here.
        val currentCropPercentages = imageCropPercentages.value ?: return
        if (actualAspectRatio > 3) {
            val originalWidthCropPercentage = currentCropPercentages.first
            val originalHeightCropPercentage = currentCropPercentages.second
            imageCropPercentages.value =
                Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
        }
//
//            // Debug.
//            Timber.i(">>>>> ImageAnalyzer imageCropPercentages : ${imageCropPercentages.value}")

        // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
        // the crop.
        val cropPercentages = imageCropPercentages.value ?: return
        val widthCropPercent = cropPercentages.first
        val heightCropPercent = cropPercentages.second
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
            else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
        }
//
//            //Debug.
//            Timber.i(">>>>> ImageAnalyzer cropPercentages : ${cropPercentages}")
//            Timber.i(">>>>> ImageAnalyzer widthCropPercent : ${widthCropPercent}")
//            Timber.i(">>>>> ImageAnalyzer heightCropPercent : ${heightCropPercent}")
//            Timber.i(">>>>> ImageAnalyzer widthCrop : ${widthCrop}")
//            Timber.i(">>>>> ImageAnalyzer heightCrop : ${heightCrop}")

        cropRect.inset(
            (imageWidth * widthCrop / 2).toInt(),
            (imageHeight * heightCrop / 2).toInt()
        )
        val croppedBitmap =
            ImageUtils.rotateAndCrop(convertImageToBitmap, rotationDegrees, cropRect)
//
//            // Debug.
//            Timber.i(">>>>> ImageAnalyzer cropRect : ${cropRect}")

        //val tfImage = TensorImage.fromBitmap(croppedBitmap)
        val tfImage = TensorImage(DataType.FLOAT32)
        tfImage.load(croppedBitmap)

        // Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 800, 600, 1), DataType.FLOAT32)

        // Debug.
        Timber.i(">>>>> ImageAnalyzer tfImage.buffer : %s", tfImage.buffer.toString())
        Timber.i(">>>>> ImageAnalyzer tfImage.tensorBuffer : ${tfImage?.tensorBuffer?.buffer.toString()}")
        Timber.i(
            ">>>>> ImageAnalyzer inputFeature0.buffer : %s",
            inputFeature0.buffer.toString()
        )
        Timber.i(">>>>> ImageAnalyzer model.buffer : %s", inputFeature0.buffer.toString())


        inputFeature0.loadBuffer(tfImage.buffer)

        // TODO 3: Process the image using the trained model, sort and pick out the top results
//            val resized = Bitmap.createScaledBitmap(croppedBitmap, 30, 30, true)
//            //val model = MyModel.newInstance(this)
//            val tImage = TensorImage(DataType.FLOAT32)
//            tImage.load(resized)
////            var tensorImage = tImage.load(resized)
//            val byteBuffer = tImage.tensorBuffer

        // Runs model inference and gets result.
//            val outputs = model.process(byteBuffer)
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val data1 = outputFeature0.floatArray
        Timber.i(">>>>> ImageAnalyzer[1] : $data1")
        Timber.i(">>>>> ImageAnalyzer[2] : ${outputFeature0.dataType}")
        Timber.i(">>>>> ImageAnalyzer[3] : ${data1[0]}")

//
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//            val converted = String(buffer.array(), "UTF-8")
//            Toast.makeText(this, "output: $outputFeature0", Toast.LENGTH_SHORT).show()
        //Releases model resources if no longer used.


//            val outputs = model.process(tfImage)
//                .probabilityAsCategoryList.apply {
//                    sortByDescending { it.score } // Sort with highest confidence first
//                }.take(MAX_RESULT_DISPLAY) // take the top results

        // TODO 4: Converting the top probability items into a list of recognitions
//            for (output in outputs) {
//                items.add(Recognition(output.label, output.score))
//            }

//            // START - Placeholder code at the start of the codelab. Comment this block of code out.
//            for (i in 0 until MAX_RESULT_DISPLAY){
//                items.add(Recognition("Fake label $i", Random.nextFloat()))
//            }
//            // END - Placeholder code at the start of the codelab. Comment this block of code out.

        // Close the image,this tells CameraX to feed the next image to the analyzer
        imageProxy.close()
    }

    /**
     * Convert Image Proxy to Bitmap
     */
    private val yuvToRgbConverter = YuvToRgbConverter(context)
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var rotationMatrix: Matrix

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

        val image = imageProxy.image ?: return null

        // Initialise Buffer
        if (!::bitmapBuffer.isInitialized) {
            // The image rotation and RGB image buffer are initialized only once
            Timber.d(">>>>> ImageAnalyzer : Initalise toBitmap()")
            rotationMatrix = Matrix()
            rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
            )
        }

        // Pass image to an image analyser
        yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

        // Create the Bitmap in the correct orientation
        return Bitmap.createBitmap(
            bitmapBuffer,
            0,
            0,
            bitmapBuffer.width,
            bitmapBuffer.height,
            rotationMatrix,
            false
        )
    }
}