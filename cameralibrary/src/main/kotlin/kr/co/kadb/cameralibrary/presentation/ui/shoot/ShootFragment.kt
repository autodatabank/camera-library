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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaActionSound
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.addListener
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootSharedViewModel.Event
import kr.co.kadb.cameralibrary.presentation.widget.extension.repeatOnStarted
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.BitmapUtils
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.GraphicOverlay
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.TextRecognitionProcessor
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.VisionImageProcessor
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.MediaActionSound2
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
        fun create() = ShootFragment()
    }

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

    //
    private var graphicOverlay: GraphicOverlay? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false

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

                // 회전에 따른 UI 변경.
                if (viewModel.item.value.canUiRotation && imageCapture?.targetRotation != rotation) {
                    initUnusedAreaLayout()
                }

                // Rotation 갱신.
                imageCapture?.targetRotation = rotation
                imageAnalyzer?.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.stop()
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

    override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.stop()
    }

    override fun onBackPressed(): Boolean {
        viewModel.stopShooting()
        return false
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
//        // 권한 확인 후 카메라 및 UI 초기화.
//        viewController.requestCameraPermission {
//            initCamera()
//            initUnusedAreaLayout()
//        }
        initLayout()

        graphicOverlay = binding.adbCameralibraryGraphicOverlay

        // 여러장 촬영 상태에서만 촬영완료 버튼 활성화.
        viewModel.item.value.isMultiplePicture.also {
            binding.adbCameralibraryLayoutFinish.isVisible = it
        }

        // 플래쉬 초기 아이콘 상태.
        val (@StringRes stringId, @DrawableRes drawableId) = when (viewModel.flashMode) {
            ImageCapture.FLASH_MODE_ON -> Pair(
                R.string.adb_cameralibrary_text_flash_on,
                R.drawable.adb_cameralibrary_selector_ic_baseline_flash_on_accent_48
            )
            ImageCapture.FLASH_MODE_AUTO -> Pair(
                R.string.adb_cameralibrary_text_flash_auto,
                R.drawable.adb_cameralibrary_selector_ic_baseline_flash_auto_accent_48
            )
            else -> Pair(
                R.string.adb_cameralibrary_text_flash_off,
                R.drawable.adb_cameralibrary_selector_ic_baseline_flash_off_white_48
            )
        }
        binding.adbCameralibraryTextviewFlash.setText(stringId)
        binding.adbCameralibraryButtonFlash.setImageResource(drawableId)
    }

    // Init Observer.
    override fun initObserver() {
        repeatOnStarted {
            viewModel.item.collect { item ->

            }
        }
        repeatOnStarted {
            viewModel.eventFlow.collect { event ->
                // Debug.
                Timber.i(">>>>> ShootFragment event collect : $event")
                when (event) {
                    is Event.PlayShutterSound -> {
                        playShutterSound(event.canMute)
                    }
                    is Event.TakePicture -> {
                        // 결과 전달.
                        Intent().apply {
                            action = IntentKey.ACTION_TAKE_PICTURE
                            putExtra("data", event.thumbnailBitmap)
                            putExtra(IntentKey.EXTRA_WIDTH, event.size.width)
                            putExtra(IntentKey.EXTRA_HEIGHT, event.size.height)
                            putExtra(IntentKey.EXTRA_ROTATION, event.rotation)
                            setDataAndType(event.uri, "image/jpeg")
                        }.also {
                            requireActivity().setResult(Activity.RESULT_OK, it)
                        }.run {
                            activity?.finish()
                            event.thumbnailBitmap?.recycle()
                        }
                    }
                    is Event.TakeMultiplePictures -> {
                        Intent().apply {
                            action = IntentKey.ACTION_TAKE_MULTIPLE_PICTURES
                            putExtra(IntentKey.EXTRA_URIS, event.uris)
                            putExtra(IntentKey.EXTRA_SIZES, event.sizes)
                            putExtra(IntentKey.EXTRA_ROTATIONS, event.rotations)
                        }.also {
                            requireActivity().setResult(Activity.RESULT_OK, it)
                        }.run {
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.adbCameralibraryButtonShooting.setOnClickListener {
            // 촬영 가능 확인.
            if (!viewModel.canTakePicture()) {
                return@setOnClickListener
            }

            // 이미지 가져오기.
            imageCapture?.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        // Debug.
                        Timber.i(">>>>> ImageCapture onCaptureSuccess")

                        try {

                            // 이미지 저장.
                            viewModel.saveImage(
                                image.planes[0].buffer,
                                image.width,
                                image.height,
                                image.imageInfo.rotationDegrees
                            )
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                        // Close ImageProxy.
                        //image.close()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        // Debug.
                        Timber.e(">>>>> OnImageSavedCallback onError: ${exception.message}")
                    }
                })
        }

        // 플래쉬.
        binding.adbCameralibraryButtonFlash.setOnClickListener {
            val (@StringRes stringId, @DrawableRes drawableId, mode) = when (viewModel.flashMode) {
                ImageCapture.FLASH_MODE_OFF -> Triple(
                    R.string.adb_cameralibrary_text_flash_on,
                    R.drawable.adb_cameralibrary_selector_ic_baseline_flash_on_accent_48,
                    ImageCapture.FLASH_MODE_ON
                )
                ImageCapture.FLASH_MODE_ON -> Triple(
                    R.string.adb_cameralibrary_text_flash_auto,
                    R.drawable.adb_cameralibrary_selector_ic_baseline_flash_auto_accent_48,
                    ImageCapture.FLASH_MODE_AUTO
                )
                else -> Triple(
                    R.string.adb_cameralibrary_text_flash_off,
                    R.drawable.adb_cameralibrary_selector_ic_baseline_flash_off_white_48,
                    ImageCapture.FLASH_MODE_OFF
                )
            }
            viewModel.flashMode = mode
            imageCapture?.flashMode = mode
            binding.adbCameralibraryTextviewFlash.setText(stringId)
            binding.adbCameralibraryButtonFlash.setImageResource(drawableId)
        }

        // 촬영종료.
        binding.adbCameralibraryButtonFinish.setOnClickListener {
            viewModel.stopShooting()
        }
    }

    // Init Callback.
    override fun initCallback() {
    }

    private fun initLayout() {
        // 권한 확인 후 카메라 및 UI 초기화.
        viewController.requestCameraPermission {
            initCamera()
            initUnusedAreaLayout()
        }
    }

    // Init Unused area layout.
    private fun initUnusedAreaLayout() {
        // 크롭 사용 시 Layout 및 Horizon 활성.
        viewModel.item.value.cropPercent.let {
            it.size == 2
        }.also {
            binding.adbCameralibraryViewUnusedAreaBorderTop.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderEnd.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderStart.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderBottom.isVisible = it
        }

        // 수평선, 크롭 사용 시 Layout Border 및 Color 설정.
        val (horizonColor, unusedAreaBorderColor) = viewModel.horizonAndUnusedAreaBorderColor()
        binding.apply {
            adbCameralibraryViewHorizon.setBackgroundColor(horizonColor)
            adbCameralibraryViewUnusedAreaBorderTop.setBackgroundColor(unusedAreaBorderColor)
            adbCameralibraryViewUnusedAreaBorderEnd.setBackgroundColor(unusedAreaBorderColor)
            adbCameralibraryViewUnusedAreaBorderStart.setBackgroundColor(unusedAreaBorderColor)
            adbCameralibraryViewUnusedAreaBorderBottom.setBackgroundColor(unusedAreaBorderColor)
        }

        // 크롭크기로 영역 지정.
        binding.adbCameralibraryLayout.post {
            val targetRotation = imageCapture?.targetRotation ?: 0
//            val previewView = binding.adbCameralibraryPreviewView
            val unusedAreaView = binding.adbCameralibraryLayoutUnusedArea
            val unusedAreaViewTop = binding.adbCameralibraryViewUnusedAreaTop
            val unusedAreaViewBottom = binding.adbCameralibraryViewUnusedAreaBottom
            val (unusedAreaWidth, unusedAreaHeight) = viewModel.unusedAreaSize(
                targetRotation, unusedAreaView.width, unusedAreaView.height
            )

            // 플래쉬, 촬영, 완료 버튼 회전.
            (targetRotation * 90).toFloat().also {
                binding.adbCameralibraryLayoutFlash.animate().rotation(it)
                binding.adbCameralibraryLayoutFinish.animate().rotation(it)
                binding.adbCameralibraryButtonShooting.animate().rotation(it)
            }
//
//            binding.adbCameralibraryGraphicOverlay.apply {
//                ConstraintSet().let {
//                    it.clone(binding.adbCameralibraryLayout)
//                    it.connect(
//                        id, ConstraintSet.TOP, previewView.id, ConstraintSet.TOP
//                    )
//                    it.connect(
//                        id, ConstraintSet.BOTTOM, previewView.id, ConstraintSet.BOTTOM
//                    )
//                    it.connect(
//                        id, ConstraintSet.START, previewView.id, ConstraintSet.START
//                    )
//                    it.connect(
//                        id, ConstraintSet.END, previewView.id, ConstraintSet.END
//                    )
//                    it.applyTo(binding.adbCameralibraryLayout)
//                }
//            }

            // 크롭 사용 시 Layout 설정.
            if (unusedAreaWidth > 0 && unusedAreaHeight > 0) {
                binding.adbCameralibraryViewUnusedAreaTop.apply {
                    layoutParams = ConstraintLayout.LayoutParams(0, unusedAreaHeight.toInt())
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(
                            id, ConstraintSet.TOP, unusedAreaView.id, ConstraintSet.TOP
                        )
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaBottom.apply {
                    layoutParams = ConstraintLayout.LayoutParams(0, unusedAreaHeight.toInt())
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(
                            id, ConstraintSet.BOTTOM, unusedAreaView.id, ConstraintSet.BOTTOM
                        )
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaStart.apply {
                    layoutParams = ConstraintLayout.LayoutParams(unusedAreaWidth.toInt(), 0)
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(
                            id, ConstraintSet.START, unusedAreaView.id, ConstraintSet.START
                        )
                        it.connect(
                            id, ConstraintSet.TOP, unusedAreaViewTop.id, ConstraintSet.BOTTOM
                        )
                        it.connect(
                            id, ConstraintSet.BOTTOM, unusedAreaViewBottom.id, ConstraintSet.TOP
                        )
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaEnd.apply {
                    layoutParams = ConstraintLayout.LayoutParams(unusedAreaWidth.toInt(), 0)
                    ConstraintSet().also {
                        it.clone(unusedAreaView)
                        it.connect(
                            id, ConstraintSet.END, unusedAreaView.id, ConstraintSet.END
                        )
                        it.connect(
                            id, ConstraintSet.TOP, unusedAreaViewTop.id, ConstraintSet.BOTTOM
                        )
                        it.connect(
                            id, ConstraintSet.BOTTOM, unusedAreaViewBottom.id, ConstraintSet.TOP
                        )
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewHorizon.apply {
                    layoutParams = ConstraintLayout.LayoutParams(1, 1)
                    ConstraintSet().also {
                        it.clone(unusedAreaView)
                        it.connect(
                            id, ConstraintSet.START, unusedAreaView.id, ConstraintSet.START
                        )
                        it.connect(
                            id, ConstraintSet.END, unusedAreaView.id, ConstraintSet.END
                        )
                        it.connect(
                            id, ConstraintSet.TOP, unusedAreaView.id, ConstraintSet.TOP
                        )
                        it.connect(
                            id, ConstraintSet.BOTTOM, unusedAreaView.id, ConstraintSet.BOTTOM
                        )
                        it.applyTo(unusedAreaView)
                    }
                }.run {
                    val transition = ChangeBounds()
                    transition.interpolator = AccelerateDecelerateInterpolator()
                    transition.addListener(onEnd = {
                        val (width, height) = when (targetRotation) {
                            1, 3 -> Pair(2, 0)
                            else -> Pair(0, 2)
                        }
                        layoutParams = ConstraintLayout.LayoutParams(width, height)
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(unusedAreaView)
                        constraintSet.connect(
                            id, ConstraintSet.START, unusedAreaView.id, ConstraintSet.START
                        )
                        constraintSet.connect(
                            id, ConstraintSet.END, unusedAreaView.id, ConstraintSet.END
                        )
                        constraintSet.connect(
                            id, ConstraintSet.TOP, unusedAreaView.id, ConstraintSet.TOP
                        )
                        constraintSet.connect(
                            id, ConstraintSet.BOTTOM, unusedAreaView.id, ConstraintSet.BOTTOM
                        )
                        constraintSet.applyTo(unusedAreaView)
                        val innerTransition = ChangeBounds()
                        innerTransition.interpolator = AccelerateDecelerateInterpolator()
                        TransitionManager.beginDelayedTransition(
                            binding.adbCameralibraryLayout, innerTransition
                        )
                    })
                    TransitionManager.beginDelayedTransition(
                        binding.adbCameralibraryLayout, transition
                    )
                }
//
//                // Debug - 해상도 표시.
//                val (cropWidth, cropHeight) = when (targetRotation) {
//                    1, 3 -> Pair(
//                        unusedAreaView.height.toFloat() - (unusedAreaHeight * 2.0f),
//                        unusedAreaView.width.toFloat() - (unusedAreaWidth * 2.0f)
//                    )
//                    else -> Pair(
//                        unusedAreaView.width.toFloat() - (unusedAreaWidth * 2.0f),
//                        unusedAreaView.height.toFloat() - (unusedAreaHeight * 2.0f)
//                    )
//                }
//                binding.adbCameralibraryTextviewDebug.text = resources.displayMetrics.density.let {
//                    "${(cropWidth * it).toInt()}x${(cropHeight * it).toInt()}"
//                }
            }
        }
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun initCamera() {
        // Wait for the views to be properly laid out
        binding.adbCameralibraryPreviewView.post {
            // Set up the camera and its use cases
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
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        // Must unbind the use-cases before rebinding them
        cameraProvider?.unbindAll()

        //
        imageProcessor?.stop()

        // rotation
        val rotation = binding.adbCameralibraryPreviewView.display.rotation
//
//        // CameraProvider
//        val cameraProvider = cameraProvider
//            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        imageProcessor = TextRecognitionProcessor(
            requireContext(),
            KoreanTextRecognizerOptions.Builder().build()
        )


        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
        // Attach the viewfinder's surface provider to preview use case
        preview?.setSurfaceProvider(binding.adbCameralibraryPreviewView.surfaceProvider)


        // ImageCapture
        imageCapture = ImageCapture.Builder()
            //.setJpegQuality(50)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .setFlashMode(viewModel.flashMode)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setTargetRotation(rotation)
            .build()
        imageAnalyzer?.setAnalyzer(
            ContextCompat.getMainExecutor(requireContext())
        ) { imageProxy: ImageProxy ->
//            if (needUpdateGraphicOverlayImageSourceInfo) {
//                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
//                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//                if (rotationDegrees == 0 || rotationDegrees == 180) {
//                    binding.adbCameralibraryGraphicOverlay.setImageSourceInfo(
//                        imageProxy.width,
//                        imageProxy.height,
//                        isImageFlipped
//                    )
//                } else {
//                    binding.adbCameralibraryGraphicOverlay.setImageSourceInfo(
//                        imageProxy.height,
//                        imageProxy.width,
//                        isImageFlipped
//                    )
//                }
//                needUpdateGraphicOverlayImageSourceInfo = false
//            }

            BitmapUtils.getBitmap(imageProxy)?.let { bitmap ->
                val image = InputImage.fromBitmap(
                    bitmap, imageProxy.imageInfo.rotationDegrees
                )
                recognizer.process(image).addOnSuccessListener { text ->
                    text.textBlocks.forEach { textBlock ->
                        textBlock.lines.forEach { textLine ->
                            textLine.elements.forEach { element ->
                                Timber.i(">>>>> recognizer addOnSuccessListener : ${element.text}")
                            }
                        }
                    }
                }.addOnFailureListener {
                    Timber.i(">>>>> recognizer addOnFailureListener : $it")
                }
            }

            Timber.i(">>>>> imageProxy.width : ${imageProxy.width}")
            Timber.i(">>>>> imageProxy.height : ${imageProxy.height}")
            Timber.i(">>>>> adbCameralibraryPreviewView.width : ${binding.adbCameralibraryPreviewView.width}")
            Timber.i(">>>>> adbCameralibraryPreviewView.height : ${binding.adbCameralibraryPreviewView.height}")
            Timber.i(">>>>> adbCameralibraryGraphicOverlay.width : ${binding.adbCameralibraryGraphicOverlay.width}")
            Timber.i(">>>>> adbCameralibraryGraphicOverlay.height : ${binding.adbCameralibraryGraphicOverlay.height}")

            try {
//                        imageProcessor?.processBitmap(bitmap, graphicOverlay)
                imageProcessor?.processImageProxy(imageProxy, graphicOverlay)
            } catch (ex: MlKitException) {
                ex.printStackTrace()
            }
        }

//        // Must unbind the use-cases before rebinding them
//        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            //
            //observeCameraState(camera?.cameraInfo!!)
        } catch (ex: Exception) {
            ex.printStackTrace()
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
//
//    // 카메라 상태 로깅.
//    private fun observeCameraState(cameraInfo: CameraInfo) {
//        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
//            when (cameraState.type) {
//                Type.PENDING_OPEN -> Timber.v(">>>>> CameraState : PENDING_OPEN")
//                Type.OPENING -> Timber.v(">>>>> CameraState : OPENING")
//                Type.OPEN -> Timber.v(">>>>> CameraState : OPEN")
//                Type.CLOSING -> Timber.v(">>>>> CameraState : CLOSING")
//                Type.CLOSED -> Timber.v(">>>>> CameraState : CLOSED")
//            }
//            when (cameraState.error ?: -1) {
//                CameraState.ERROR_STREAM_CONFIG -> {
//                    Timber.e(">>>>> CameraState : ERROR_STREAM_CONFIG")
//                }
//                CameraState.ERROR_CAMERA_IN_USE -> {
//                    Timber.e(">>>>> CameraState : ERROR_CAMERA_IN_USE")
//                }
//                CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
//                    Timber.e(">>>>> CameraState : ERROR_MAX_CAMERAS_IN_USE")
//                }
//                CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
//                    Timber.e(">>>>> CameraState : ERROR_OTHER_RECOVERABLE_ERROR")
//                }
//                CameraState.ERROR_CAMERA_DISABLED -> {
//                    Timber.e(">>>>> CameraState : ERROR_CAMERA_DISABLED")
//                }
//                CameraState.ERROR_CAMERA_FATAL_ERROR -> {
//                    Timber.e(">>>>> CameraState : ERROR_CAMERA_FATAL_ERROR")
//                }
//                CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
//                    Timber.e(">>>>> CameraState : ERROR_DO_NOT_DISTURB_MODE_ENABLED")
//                }
//            }
//        }
//    }

    // 셔터음.
    private fun playShutterSound(canMute: Boolean) {
        if (canMute) {
            // 미디어 볼륨으로 셔터효과음 재생.
            mediaActionSound.playWithStreamVolume(
                MediaActionSound.SHUTTER_CLICK,
                audioManager
            )
        } else {
            // 최소 볼륨으로 셔터효과음 재생.
            mediaActionSound.playWithMinimumVolume(
                MediaActionSound.SHUTTER_CLICK
            )
        }
    }
}