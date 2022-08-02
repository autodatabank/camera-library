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
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.camera.core.CameraState.Type
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.addListener
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.extension.exif
import kr.co.kadb.cameralibrary.presentation.widget.extension.exifInterface
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
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
        Intent(context, ShootActivity::class.java)
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
        // 권한 확인 후 카메라 및 UI 초기화.
        viewController.requestCameraPermission {
            initCamera()
            initUnusedAreaLayout()
        }

        // 여러장 촬영 상태에서만 촬영 완료 버튼 활성화.
        viewModel.item.value.isMultiplePicture.also {
            binding.adbCameralibraryButtonFinish.isVisible = it
            binding.adbCameralibraryTextviewFinish.isVisible = it
        }

        // 플래쉬 초기 아이콘 상태.
        when (viewModel.flashMode) {
            ImageCapture.FLASH_MODE_OFF -> binding.adbCameralibraryButtonFlash.setImageResource(
                R.drawable.adb_cameralibrary_ic_baseline_flash_off_white_48
            )
            ImageCapture.FLASH_MODE_ON -> binding.adbCameralibraryButtonFlash.setImageResource(
                R.drawable.adb_cameralibrary_ic_baseline_flash_on_white_48
            )
            ImageCapture.FLASH_MODE_AUTO -> binding.adbCameralibraryButtonFlash.setImageResource(
                R.drawable.adb_cameralibrary_ic_baseline_flash_auto_white_48
            )
        }
    }

    // Init Observer.
    override fun initObserver() {
        lifecycleScope.launch {
            viewModel.item.collect {
                // Debug.
                Timber.i(">>>>>>>>>> ShootFragment item collect : $it")
            }
        }
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.adbCameralibraryButtonShooting.setOnClickListener {
            // Debug.
            Timber.d(">>>>> Shooting OnClickListener")
            //
            viewModel.item.value.also {
                if (it.isShooted && !it.isMultiplePicture) {
                    return@setOnClickListener
                }
            }

            //
            viewModel.pressedShutter()
//
//            lifecycleScope.launch {
//                for (i in 0..1000) {
//                    Timber.i(">>>>> CoroutineScope sleeping : $i => %s", cameraExecutor.toString())
//                    Timber.i(">>>>> CoroutineScope sleeping : $i => %s", cameraExecutor.isTerminated)
//                    Timber.i(">>>>> CoroutineScope sleeping : $i => %s", cameraExecutor.isShutdown)
//                    delay(10L)
//                }
//            }



            imageCapture?.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        // Debug.
                        Timber.i(">>>>> ImageCapture onCaptureSuccess")

                        if (viewModel.item.value.canMute) {
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

////                        val imageFile = requireContext().createFile(true)
////                        val output = FileOutputStream(imageFile)
//                        val buffer = image.planes[0].buffer
//                        val uri = ByteArray(buffer.capacity()).also {
//                            buffer.get(it)
//                        }.let {
//                            it.save(requireContext(), true)?.toUri()
//                        }
////                        output.write(bytes)

                        val uri = viewModel.toUri(image.planes[0].buffer)

                        // Exif Logging.
                        //val uri = imageFile?.toUri()
                        val exif = uri?.exif(requireContext())
                        val exifInterface = uri?.exifInterface(requireContext())


                        // Result.
                        if (viewModel.item.value.isMultiplePicture) {
                            viewModel.pressedShutter(uri, exif?.width, exif?.height)
                        } else {
                            // Thumbnail Bitmap.
                            val thumbnail = uri?.toThumbnail(requireContext(), exif)
                            // 결과 전달.
                            Intent().apply {
                                action = viewModel.item.value.action
                                putExtra("data", thumbnail)
                                putExtra(IntentKey.EXTRA_WIDTH, exif?.width)
                                putExtra(IntentKey.EXTRA_HEIGHT, exif?.height)
                                setDataAndType(uri, "image/jpeg")
                            }.also {
                                requireActivity().setResult(Activity.RESULT_OK, it)
                            }.run {
                                activity?.finish()
                                thumbnail?.recycle()
                            }
                        }

                        image.close()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        // Debug.
                        Timber.e(">>>>> OnImageSavedCallback onError: ${exception.message}")
                    }
                })

//            // Get a stable reference of the modifiable image capture use case
//            imageCapture?.let { imageCapture ->
//                // 출력 옵션.
//                val outputOptions = viewModel.outputFileOptions(lensFacing)
//
//                // 촬영 후 이미지 저장.
//                imageCapture.takePicture(outputOptions, cameraExecutor,
//                    object : ImageCapture.OnImageSavedCallback {
//                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            if (viewModel.item.value.canMute) {
//                                // 미디어 볼륨으로 셔터효과음 재생.
//                                mediaActionSound.playWithStreamVolume(
//                                    MediaActionSound.SHUTTER_CLICK,
//                                    audioManager
//                                )
//                            } else {
//                                // 최소 볼륨으로 셔터효과음 재생.
//                                mediaActionSound.playWithMinimumVolume(
//                                    MediaActionSound.SHUTTER_CLICK
//                                )
//                            }
//
//                            // Debug.
//                            Timber.i(">>>>> OnImageSavedCallback onImageSaved: ${output.savedUri}")
//
//                            // Exif Logging.
//                            val exif = output.savedUri?.exif(requireContext())
//                            val exifInterface = output.savedUri?.exifInterface(requireContext())
//
//                            // Result.
//                            if (viewModel.item.value.isMultiplePicture) {
//                                viewModel.pressedShutter(output.savedUri, exif?.width, exif?.height)
//                            } else {
//                                // Thumbnail Bitmap.
//                                val thumbnail = output.savedUri?.toThumbnail(requireContext(), exif)
//                                // 결과 전달.
//                                Intent().apply {
//                                    action = viewModel.item.value.action
//                                    putExtra("data", thumbnail)
//                                    putExtra(IntentKey.EXTRA_WIDTH, exif?.width)
//                                    putExtra(IntentKey.EXTRA_HEIGHT, exif?.height)
//                                    setDataAndType(output.savedUri, "image/jpeg")
//                                }.also {
//                                    requireActivity().setResult(Activity.RESULT_OK, it)
//                                }.run {
//                                    activity?.finish()
//                                    thumbnail?.recycle()
//                                }
//                            }
//                        }
//
//                        override fun onError(exception: ImageCaptureException) {
//                            Timber.e(">>>>> OnImageSavedCallback onError: ${exception.message}")
//                        }
//                    })
//            }

        }

        // 플래쉬.
        binding.adbCameralibraryButtonFlash.setOnClickListener {
            viewModel.flashMode = when (viewModel.flashMode) {
                ImageCapture.FLASH_MODE_OFF -> {
                    (it as ImageButton).setImageResource(
                        R.drawable.adb_cameralibrary_ic_baseline_flash_on_white_48
                    )
                    ImageCapture.FLASH_MODE_ON
                }
                ImageCapture.FLASH_MODE_ON -> {
                    (it as ImageButton).setImageResource(
                        R.drawable.adb_cameralibrary_ic_baseline_flash_auto_white_48
                    )
                    ImageCapture.FLASH_MODE_AUTO
                }
                else -> {
                    (it as ImageButton).setImageResource(
                        R.drawable.adb_cameralibrary_ic_baseline_flash_off_white_48
                    )
                    ImageCapture.FLASH_MODE_OFF
                }

            }
            imageCapture?.flashMode = viewModel.flashMode
        }

        // 촬영종료.
        binding.adbCameralibraryButtonFinish.setOnClickListener {
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
        // 크롭지정 시 영역 표시 라인 활성화.
        viewModel.item.value.cropPercent.let {
            it.size == 2
        }.also {
            binding.adbCameralibraryViewUnusedAreaLineTop.isVisible = it
            binding.adbCameralibraryViewUnusedAreaLineEnd.isVisible = it
            binding.adbCameralibraryViewUnusedAreaLineStart.isVisible = it
            binding.adbCameralibraryViewUnusedAreaLineBottom.isVisible = it
        }

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
                    ConstraintSet().also {
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
                binding.adbCameralibraryViewUnusedAreaHorizon.apply {
                    layoutParams = ConstraintLayout.LayoutParams(1, 1)
                    ConstraintSet().also {
                        it.clone(binding.adbCameralibraryViewUnusedArea)
                        it.connect(
                            id,
                            ConstraintSet.START,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.START
                        )
                        it.connect(
                            id,
                            ConstraintSet.END,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.END
                        )
                        it.connect(
                            id,
                            ConstraintSet.TOP,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.TOP
                        )
                        it.connect(
                            id,
                            ConstraintSet.BOTTOM,
                            binding.adbCameralibraryViewUnusedArea.id,
                            ConstraintSet.BOTTOM
                        )
                        it.applyTo(binding.adbCameralibraryViewUnusedArea)
                    }
                }.run {
                    val transition = ChangeBounds()
                    transition.interpolator = AccelerateDecelerateInterpolator()
                    transition.addListener(onEnd = {
                        val parentView = binding.adbCameralibraryViewUnusedArea
                        val (width, height) = when (imageCapture?.targetRotation ?: 0) {
                            1, 3 -> Pair(2, 0)
                            else -> Pair(0, 2)
                        }
                        layoutParams = ConstraintLayout.LayoutParams(width, height)
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(parentView)
                        constraintSet.connect(
                            id,
                            ConstraintSet.START,
                            parentView.id,
                            ConstraintSet.START
                        )
                        constraintSet.connect(
                            id,
                            ConstraintSet.END,
                            parentView.id,
                            ConstraintSet.END
                        )
                        constraintSet.connect(
                            id,
                            ConstraintSet.TOP,
                            parentView.id,
                            ConstraintSet.TOP
                        )
                        constraintSet.connect(
                            id,
                            ConstraintSet.BOTTOM,
                            parentView.id,
                            ConstraintSet.BOTTOM
                        )
                        constraintSet.applyTo(parentView)
                        val innerTransition = ChangeBounds()
                        innerTransition.interpolator = AccelerateDecelerateInterpolator()
                        TransitionManager.beginDelayedTransition(
                            binding.adbCameralibraryViewUnusedArea, innerTransition
                        )
                    })
                    TransitionManager.beginDelayedTransition(
                        binding.adbCameralibraryViewUnusedArea, transition
                    )
                }
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
//
//    private var cameraDevice: CameraDevice? = null
//
//    /** [HandlerThread] where all camera operations run */
//    private val cameraThread = HandlerThread("CameraThread").apply { start() }
//
//    /** [Handler] corresponding to [cameraThread] */
//    private val cameraHandler = Handler(cameraThread.looper)

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {
        // rotation
        val rotation = binding.adbCameralibraryPreviewView.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
//
//
//        val manager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        manager.registerTorchCallback(cameraExecutor, object : CameraManager.TorchCallback() {
//            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
//                super.onTorchModeChanged(cameraId, enabled)
//                Timber.i(">>>>>>>>>> TorchCallback onTorchModeChanged")
//            }
//
//            override fun onTorchModeUnavailable(cameraId: String) {
//                super.onTorchModeUnavailable(cameraId)
//                Timber.i(">>>>>>>>>> TorchCallback onTorchModeUnavailable")
//            }
//        })
//
//
//        // Create the callback you want to attach to the Preview use case
//        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
//            override fun onCaptureBufferLost(
//                session: CameraCaptureSession,
//                request: CaptureRequest,
//                target: Surface,
//                frameNumber: Long
//            ) {
//                super.onCaptureBufferLost(session, request, target, frameNumber)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureBufferLost")
//            }
//
//            override fun onCaptureFailed(
//                session: CameraCaptureSession,
//                request: CaptureRequest,
//                failure: CaptureFailure
//            ) {
//                super.onCaptureFailed(session, request, failure)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureFailed")
//            }
//
//            override fun onCaptureProgressed(
//                session: CameraCaptureSession,
//                request: CaptureRequest,
//                partialResult: CaptureResult
//            ) {
//                super.onCaptureProgressed(session, request, partialResult)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureProgressed")
//            }
//
//            override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
//                super.onCaptureSequenceAborted(session, sequenceId)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureSequenceAborted")
//            }
//
//            override fun onCaptureSequenceCompleted(
//                session: CameraCaptureSession,
//                sequenceId: Int,
//                frameNumber: Long
//            ) {
//                super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureSequenceCompleted")
//            }
//
//            override fun onCaptureStarted(
//                session: CameraCaptureSession,
//                request: CaptureRequest,
//                timestamp: Long,
//                frameNumber: Long
//            ) {
//                super.onCaptureStarted(session, request, timestamp, frameNumber)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureStarted : $session")
//            }
//
//            override fun onCaptureCompleted(
//                session: CameraCaptureSession,
//                request: CaptureRequest,
//                result: TotalCaptureResult
//            ) {
//                super.onCaptureCompleted(session, request, result)
//                Timber.i(">>>>>>>>>> CaptureCallback onCaptureCompleted : ${session.device}")
//            }
//        }
//
//
//        //CaptureRequest.Builder()
//        var captureRequestBuilder: CaptureRequest.Builder? = null
//
//        // Set up and configure the Preview's builder
//        val previewBuilder = Preview.Builder()
//
//        val stateCallback = object : CameraCaptureSession.StateCallback() {
//            override fun onConfigured(session: CameraCaptureSession) {
//                captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//                captureRequestBuilder.addTarget(binding.adbCameralibraryPreviewView.surfaceProvider.)
//                session.setRepeatingRequest(
//                    captureRequestBuilder?.build()!!,
//                    captureCallback,
//                    cameraHandler
//                )
//                Timber.i(">>>>>>>>>> StateCallback onConfigured")
//            }
//
//            override fun onConfigureFailed(session: CameraCaptureSession) {
//                Timber.i(">>>>>>>>>> StateCallback onConfigureFailed")
//            }
//        }
//
//        val deviceStateCallback = object : CameraDevice.StateCallback() {
//            override fun onOpened(camera: CameraDevice) {
//                cameraDevice = camera
//                Timber.i(">>>>>>>>>> CameraDevice.StateCallback onOpened")
//            }
//
//            override fun onError(camera: CameraDevice, error: Int) {
//                cameraDevice?.close()
//                cameraDevice = null
//                Timber.i(">>>>>>>>>> CameraDevice.StateCallback onError")
//            }
//
//            override fun onDisconnected(camera: CameraDevice) {
//                cameraDevice?.close()
//            }
//        }
//
//        // Create an Extender to attach Camera2 options
//        val previewExtender = Camera2Interop.Extender(previewBuilder)
////        //previewExtender.setCaptureRequestTemplate(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)
//
//        previewExtender.setSessionStateCallback(stateCallback)
//        previewExtender.setDeviceStateCallback(deviceStateCallback)
//        // Attach the Camera2 CaptureCallback
//        //previewExtender.setSessionCaptureCallback(captureCallback)

        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()


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
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
//            .also {
//                it.setAnalyzer(cameraExecutor) { imageProxy ->
//                    Timber.i(">>>>>>>>>> imageAnalyzer")
//                    imageProxy.close()
//                }
//            }

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

//
//
//            camera?.cameraControl?.enableTorch(true)?.addListener({
//                Timber.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> enableTorch")
//            }, ContextCompat.getMainExecutor(requireContext()))
//            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
//                .createPoint(.5f, .5f)
//            val autoFocusAction = FocusMeteringAction.Builder(
//                autoFocusPoint,
//                FocusMeteringAction.FLAG_AF
//            ).apply {
//                //start auto-focusing after 2 seconds
//                setAutoCancelDuration(2, TimeUnit.SECONDS)
//            }.build()
//            val focusListenableFuture = camera?.cameraControl?.startFocusAndMetering(autoFocusAction)?.addListener({
//                Timber.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> startFocusAndMetering")
//            }, ContextCompat.getMainExecutor(requireContext()))
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