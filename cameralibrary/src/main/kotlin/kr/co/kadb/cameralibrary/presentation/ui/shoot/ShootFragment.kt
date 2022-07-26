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
import androidx.camera.core.*
import androidx.camera.core.CameraState.Type
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.extension.exif
import kr.co.kadb.cameralibrary.presentation.widget.extension.toThumbnail
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.MediaActionSound2
import timber.log.Timber
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

                if (viewModel.item.value.canUiRotation && imageCapture?.targetRotation != rotation) {
                    initUnusedAreaLayout()
                }

                //preview?.targetRotation = rotation
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

    override fun onBackPressed(): Boolean {
        Intent().apply {
            action = viewModel.item.value.action
            putExtra(IntentKey.EXTRA_URIS, viewModel.item.value.uris)
            putExtra(IntentKey.EXTRA_SIZES, viewModel.item.value.sizes)
        }.also {
            requireActivity().setResult(Activity.RESULT_OK, it)
        }
        return super.onBackPressed()
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
            initCamera()
            initUnusedAreaLayout()
        }
    }

    // Init Observer.
    override fun initObserver() {
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.adbCameralibraryButtonShooting.setOnClickListener {
            //
            viewModel.item.value.also {
                if (it.isShooted && !it.isMultiplePicture) {
                    return@setOnClickListener
                }
                if (it.canMute) {
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

            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->
                // 출력 옵션.
                val outputOptions = viewModel.outputFileOptions(lensFacing)

                // 촬영 후 이미지 저장.
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Timber.e(">>>>> ImageCapture onError: ${exc.message}")
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            // Debug.
                            Timber.i(">>>>> ImageCapture onImageSaved: ${output.savedUri}")

                            // Exif Logging.
                            val exif = output.savedUri?.exif(requireContext())

                            // Result.
                            if (viewModel.item.value.isMultiplePicture) {
                                viewModel.pressedShutter(output.savedUri, exif?.width, exif?.height)
                            } else {
                                // Thumbnail Bitmap.
                                val thumbnail = output.savedUri?.toThumbnail(requireContext(), exif)
                                // 결과 전달.
                                Intent().apply {
                                    action = viewModel.item.value.action
                                    putExtra("data", thumbnail)
                                    putExtra(IntentKey.EXTRA_WIDTH, exif?.width)
                                    putExtra(IntentKey.EXTRA_HEIGHT, exif?.height)
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

        // 플래쉬.
        binding.adbCameralibraryButtonFlash.setOnClickListener {
            Intent().apply {
                action = viewModel.item.value.action
                putExtra(IntentKey.EXTRA_URIS, viewModel.item.value.uris)
                putExtra(IntentKey.EXTRA_SIZES, viewModel.item.value.sizes)
            }.also {
                requireActivity().setResult(Activity.RESULT_OK, it)
            }.run {
                activity?.finish()
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }

    // Init Unused area layout.
    private fun initUnusedAreaLayout() {
        // Debug.
        Timber.i(">>>>> initUnusedAreaLayout")
        binding.adbCameralibraryViewUnusedArea.post {
            val (unusedAreaWidth, unusedAreaHeight) = viewModel.unusedAreaSize(
                imageCapture?.targetRotation ?: 0,
                binding.adbCameralibraryViewUnusedArea.width,
                binding.adbCameralibraryViewUnusedArea.height
            )
            if (unusedAreaWidth > 0 && unusedAreaHeight > 0) {
                binding.adbCameralibraryViewUnusedAreaTop.apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        0,
                        unusedAreaHeight
                    )
                    ConstraintSet().let {
                        it.clone(binding.adbCameralibraryViewUnusedArea)
                        it.connect(
                            id,
                            ConstraintSet.TOP,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.TOP
                        )
                        it.applyTo(binding.adbCameralibraryViewUnusedArea)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaBottom.apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        0,
                        unusedAreaHeight
                    )
                    ConstraintSet().let {
                        it.clone(binding.adbCameralibraryViewUnusedArea)
                        it.connect(
                            id,
                            ConstraintSet.BOTTOM,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.BOTTOM
                        )
                        it.applyTo(binding.adbCameralibraryViewUnusedArea)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaStart.apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        unusedAreaWidth,
                        0
                    )
                    ConstraintSet().let {
                        it.clone(binding.adbCameralibraryViewUnusedArea)
                        it.connect(
                            id,
                            ConstraintSet.START,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.START
                        )
                        it.connect(
                            id,
                            ConstraintSet.TOP,
                            binding.adbCameralibraryViewUnusedAreaTop.id,
                            ConstraintSet.BOTTOM
                        )
                        it.connect(
                            id,
                            ConstraintSet.BOTTOM,
                            binding.adbCameralibraryViewUnusedAreaBottom.id,
                            ConstraintSet.TOP
                        )
                        it.applyTo(binding.adbCameralibraryViewUnusedArea)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaEnd.apply {
                    layoutParams = ConstraintLayout.LayoutParams(
                        unusedAreaWidth,
                        0
                    )
                    ConstraintSet().let {
                        it.clone(binding.adbCameralibraryViewUnusedArea)
                        it.connect(
                            id,
                            ConstraintSet.END,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.END
                        )
                        it.connect(
                            id,
                            ConstraintSet.TOP,
                            binding.adbCameralibraryViewUnusedAreaTop.id,
                            ConstraintSet.BOTTOM
                        )
                        it.connect(
                            id,
                            ConstraintSet.BOTTOM,
                            binding.adbCameralibraryViewUnusedAreaBottom.id,
                            ConstraintSet.TOP
                        )
                        it.applyTo(binding.adbCameralibraryViewUnusedArea)
                    }
                }
            }

            // Debug.
            Timber.i(">>>>> initLayout rotation : ${imageCapture?.targetRotation}")
            Timber.i(">>>>> initLayout previewView width : ${binding.adbCameralibraryViewUnusedArea.width}")
            Timber.i(">>>>> initLayout previewView height : ${binding.adbCameralibraryViewUnusedArea.height}")
            Timber.i(">>>>> initLayout unusedArea width : $unusedAreaWidth")
            Timber.i(">>>>> initLayout unusedArea height : $unusedAreaHeight")
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
        // rotation
        val rotation = binding.adbCameralibraryPreviewView.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.adbCameralibraryPreviewView.surfaceProvider)
            //
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            // Debug.
            Timber.e(">>>>> Use case binding failed: $exc")
        }
    }

    // 카메라 상태 로깅.
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
}