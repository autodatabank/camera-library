/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.media.AudioManager
import android.media.MediaActionSound
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.camera.core.*
import androidx.camera.core.CameraState.Type
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.ml.LiteModelRosettaDr1
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.extension.exif
import kr.co.kadb.cameralibrary.presentation.widget.extension.thumbnail
import kr.co.kadb.cameralibrary.presentation.widget.util.ImageUtils
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.MediaActionSound2
import kr.co.kadb.cameralibrary.presentation.widget.util.YuvToRgbConverter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Simple Data object with two fields for the label and probability
 */
data class Recognition(val label: String, val confidence: Float) {

    // For easy logging
    override fun toString(): String {
        return "$label / $probabilityString"
    }

    // Output probability as a string to enable easy data binding
    val probabilityString = String.format("%.1f%%", confidence * 100.0f)

}

// Listener for the result of the ImageAnalyzer
typealias RecognitionListener = (recognition: List<Recognition>) -> Unit

/** Helper type alias used for analysis use case callbacks */
internal typealias LumaListener = (luma: Double) -> Unit

/**
 * Modified by oooobang on 2022. 7. 16..
 * Fragment for this app. Implements all camera operations including:
 * - Viewfinder
 * - Photo taking
 * - Image analysis
 */
internal class ShootFragment :
    BaseBindingFragment<AdbCameralibraryFragmentShootBinding, ShootSharedViewModel>() {
    companion object {
        //private const val RATIO_4_3_VALUE = 4.0 / 3.0
        //private const val RATIO_16_9_VALUE = 16.0 / 9.0
        fun create() = ShootFragment()
    }

    // SharedPreferences.
    lateinit var preferences: PreferenceManager

    // ViewController.
    private val viewController: ShootController by lazy {
        ShootController(requireActivity())
    }

    // ViewModel.
    override val viewModel: ShootSharedViewModel by activityViewModels {
        ShootSharedViewModelFactory(requireContext())
    }

    // Fragment Layout.
    override val layoutResourceId: Int = R.layout.adb_cameralibrary_fragment_shoot

    // MediaActionSound2.
    private lateinit var mediaActionSound: MediaActionSound2

    // AudioManager.
    private val audioManager by lazy {
        context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    // OrientationEventListener
    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                preview?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
                imageAnalyzer?.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        mediaActionSound.release()
    }

    // Init Variable.
    override fun initVariable() {
        // Initialize Background Executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize MediaActionSound
        mediaActionSound = MediaActionSound2().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }

    // Init Layout.
    override fun initLayout(view: View) {
        // 카메라 권한 요청.
        viewController.requestCameraPermission {
            // Granted.
            Timber.i(">>>>> requestCameraPermission Granted")

            initCamera()
        }
    }

    // Init Observer.
    override fun initObserver() {
        viewModel.imageCropPercentages.observe(viewLifecycleOwner) {
            // Debug.
            Timber.i(">>>>> ShootFragment imageCropPercentages : $it")
            //drawOverlay(overlay.holder, it.first, it.second)
        }
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.buttonShooting.setOnClickListener {
            //
            viewModel.item.value.also {
                if (it.isShooted && !it.isMultiplePicture) {
                    return@setOnClickListener
                } else {
                    viewModel.pressedShutter()
                }
            }

//            // 미디어 볼륨으로 셔터효과음 재생.
//            mediaActionSound.playWithStreamVolume(
//                MediaActionSound.SHUTTER_CLICK,
//                audioManager
//            )

            // 최소 볼륨으로 셔터효과음 재생.
            mediaActionSound.playWithMinimumVolume(
                MediaActionSound.SHUTTER_CLICK/*,
                audioManager*/
            )

            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->
                //
                val outputOptions = viewModel.outputFileOptions(lensFacing)

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Timber.e(">>>>> Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            // Debug.
                            Timber.i(">>>>> Photo capture succeeded: ${output.savedUri}")

                            // Exif Logging.
                            val exif = output.savedUri?.exif(requireContext())

                            // Thumbnail Bitmap.
                            val thumbnail = output.savedUri?.thumbnail(requireContext(), exif)

                            // Result.
                            if (viewModel.item.value.isMultiplePicture) {
                                // TODO:
                            } else {
                                Intent().apply {
                                    action = viewModel.item.value.action
                                    putExtra(IntentKey.EXTRA_DATA, thumbnail)
                                    putExtra(IntentKey.EXTRA_WIDTH, exif?.width)
                                    putExtra(IntentKey.EXTRA_WIDTH, exif?.height)
                                    setDataAndType(output.savedUri, "image/jpeg")
                                }.also {
                                    requireActivity().setResult(Activity.RESULT_OK, it)
                                }.run {
                                    activity?.finish()
                                    thumbnail?.recycle()
                                }
                            }
                        }
                    })
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }

    private fun initCamera() {
        // Wait for the views to be properly laid out
        binding.previewView.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        // rotation
        val rotation = binding.previewView.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            //.setTargetAspectRatio(screenAspectRatio)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()


        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            //.setTargetAspectRatio(screenAspectRatio)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
        AspectRatio.RATIO_4_3
        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            //.setTargetAspectRatio(screenAspectRatio)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also { imageAnalysis ->
//                imageAnalysis.setAnalyzer(cameraExecutor, LuminosityAnalyzer {
//                    // Values returned from our analyzer are passed to the attached listener
//                    // We log image analysis results here - you should do something useful
//                    // instead!
//                    // Debug.
//                    //Timber.v(">>>>> Average luminosity: $luma")
//                })
                imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalyzer(
                    requireContext(),
                    viewModel.imageCropPercentages
                ) {
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.previewView.surfaceProvider)
            //
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            // Debug.
            Timber.e(">>>>> Use case binding failed: $exc")
        }
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
            when (cameraState.type) {
                Type.PENDING_OPEN -> Timber.v(">>>>> CameraState : PENDING_OPEN")
                Type.OPENING -> Timber.v(">>>>> CameraState : OPENING")
                Type.OPEN -> Timber.v(">>>>> CameraState : OPEN")
                Type.CLOSING -> Timber.v(">>>>> CameraState : CLOSING")
                Type.CLOSED -> Timber.v(">>>>> CameraState : CLOSED")
            }
            when (cameraState.error ?: -1) {
                CameraState.ERROR_STREAM_CONFIG -> {
                    Timber.e(">>>>> CameraState : ERROR_STREAM_CONFIG")
                }
                CameraState.ERROR_CAMERA_IN_USE -> {
                    Timber.e(">>>>> CameraState : ERROR_CAMERA_IN_USE")
                }
                CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                    Timber.e(">>>>> CameraState : ERROR_MAX_CAMERAS_IN_USE")
                }
                CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                    Timber.e(">>>>> CameraState : ERROR_OTHER_RECOVERABLE_ERROR")
                }
                CameraState.ERROR_CAMERA_DISABLED -> {
                    Timber.e(">>>>> CameraState : ERROR_CAMERA_DISABLED")
                }
                CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                    Timber.e(">>>>> CameraState : ERROR_CAMERA_FATAL_ERROR")
                }
                CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                    Timber.e(">>>>> CameraState : ERROR_DO_NOT_DISTURB_MODE_ENABLED")
                }
            }
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private class ImageAnalyzer(
        context: Context,
        private val imageCropPercentages: MutableLiveData<Pair<Int, Int>>,
        private val listener: RecognitionListener
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

            val items = mutableListOf<Recognition>()
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

            val tfImage = TensorImage.fromBitmap(croppedBitmap)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 1, 32, 100), DataType.FLOAT32)

            // Debug.
            Timber.i(">>>>> ImageAnalyzer tfImage.buffer : %s", tfImage.buffer.toString())
            Timber.i(">>>>> ImageAnalyzer tfImage.tensorBuffer.shape : ${tfImage?.tensorBuffer?.buffer.toString()}")
            Timber.i(">>>>> ImageAnalyzer inputFeature0.buffer : %s", inputFeature0.buffer.toString())


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

            // Return the result
            listener(items.toList())

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

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }
}