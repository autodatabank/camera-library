package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Bundle
import android.util.Size
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
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootSharedViewModel.Event
import kr.co.kadb.cameralibrary.presentation.widget.extension.repeatOnStarted
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.*
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.MediaActionSound2
import timber.log.Timber
import java.io.IOException
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

    // 이미지 분석 Overlay.
    private val detectOverlay by lazy {
        binding.adbCameralibraryGraphicOverlay
    }

    // AudioManager.
    private val audioManager by lazy {
        context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    // MediaActionSound2.
    private val mediaActionSound = MediaActionSound2().apply {
        load(MediaActionSound.SHUTTER_CLICK)
    }

    // Camera ExecutorService.
    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    // Images vision detectors.
    private var imageProcessor: VisionImageProcessor<*>? = null

    // Orientation EventListener.
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
                //imageAnalyzer?.targetRotation = rotation
                detectOverlay.rotation = rotation.toFloat()
            }
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onStart() {
        super.onStart()
        // Enable orientation listener.
        orientationEventListener.enable()

        // 권한 확인 후 카메라 및 UI 초기화.
        viewController.requestCameraPermission {
            initCamera()
            initUnusedAreaLayout()
        }
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.run { this.stop() }
    }

    override fun onStop() {
        super.onStop()

        // Disable orientation listener.
        orientationEventListener.disable()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        mediaActionSound.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }

    override fun onBackPressed(): Boolean {
        viewModel.stopShooting()
        return false
    }

    // Init Variable.
    override fun initVariable() {
    }

    // Init Layout.
    override fun initLayout(view: View) {
        // 수평선 사용 시에만 활성화.
        viewModel.item.value.hasHorizon.also {
            binding.adbCameralibraryViewHorizon.isVisible = it
        }

        // 수평선 Color 설정.
        viewModel.item.value.horizonColor.also { color ->
            binding.adbCameralibraryViewHorizon.setBackgroundColor(color)
        }

        // 여러장 촬영 상태에서만 촬영완료 버튼 활성화.
        viewModel.item.value.isMultiplePicture.also {
            binding.adbCameralibraryLayoutFinish.isVisible = it
        }

        // 주행거리, 차대번호 감지 상태에서는 플래쉬, 촬영버튼 비활성화.
        if (viewModel.item.value.isMileagePicture || viewModel.item.value.isVinNumberPicture) {
            binding.adbCameralibraryLayoutFlash.isVisible = false
            binding.adbCameralibraryButtonShooting.isVisible = false
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
        // Item collect.
        repeatOnStarted {
            viewModel.item.collect {
            }
        }

        // Event collect.
        repeatOnStarted {
            viewModel.eventFlow.collect { event ->
                // Debug.
                Timber.i(">>>>> ShootFragment event collect : $event")
                when (event) {
                    // 셔터음.
                    is Event.PlayShutterSound -> {
                        playShutterSound(event.canMute)
                    }
                    // 한 장 촬영 결과 전달.
                    is Event.TakePicture -> {
                        Intent().apply {
                            action = viewModel.item.value.action
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
                    // 여러 장 촬영 결과 전달.
                    is Event.TakeMultiplePictures -> {
                        Intent().apply {
                            action = viewModel.item.value.action
                            putExtra(IntentKey.EXTRA_URIS, event.uris)
                            putExtra(IntentKey.EXTRA_SIZES, event.sizes)
                            putExtra(IntentKey.EXTRA_ROTATIONS, event.rotations)
                        }.also {
                            requireActivity().setResult(Activity.RESULT_OK, it)
                        }.run {
                            activity?.finish()
                        }
                    }
                    // 이미지 감지 결과 전달.
                    is Event.DetectInImage -> {
                        Intent().apply {
                            action = viewModel.item.value.action
                            putExtra(IntentKey.EXTRA_DETECT_TEXT, event.text)
                            putExtra(IntentKey.EXTRA_DETECT_RECT, event.rect)
                            putExtra("data", event.thumbnailBitmap)
                            putExtra(IntentKey.EXTRA_WIDTH, event.size?.width)
                            putExtra(IntentKey.EXTRA_HEIGHT, event.size?.height)
                            putExtra(IntentKey.EXTRA_ROTATION, event.rotation)
                            setDataAndType(event.uri, "image/jpeg")
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
            takePicture()
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

    // Init unused area layout.
    private fun initUnusedAreaLayout() {
        // 크롭 사용 유무에 따라 UnusedAreaLayout 활성 상태 변경.
        viewModel.item.value.cropSize.isNotEmpty.also {
            binding.adbCameralibraryViewUnusedAreaBorderTop.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderEnd.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderStart.isVisible = it
            binding.adbCameralibraryViewUnusedAreaBorderBottom.isVisible = it
        }

        // 크롭 사용 시 Layout Border Color 설정.
        viewModel.item.value.unusedAreaBorderColor.also { color ->
            binding.adbCameralibraryViewUnusedAreaBorderTop.setBackgroundColor(color)
            binding.adbCameralibraryViewUnusedAreaBorderEnd.setBackgroundColor(color)
            binding.adbCameralibraryViewUnusedAreaBorderStart.setBackgroundColor(color)
            binding.adbCameralibraryViewUnusedAreaBorderBottom.setBackgroundColor(color)
        }

        // 크롭크기로 영역 지정.
        binding.adbCameralibraryLayout.post {
            val topSide = ConstraintSet.TOP
            val endSide = ConstraintSet.END
            val startSide = ConstraintSet.START
            val bottomSide = ConstraintSet.BOTTOM
            val targetRotation = imageCapture?.targetRotation ?: 0
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

            // 크롭 사용 시 Layout 설정.
            if (unusedAreaWidth > 0 && unusedAreaHeight > 0) {
                binding.adbCameralibraryViewUnusedAreaTop.apply {
                    layoutParams = ConstraintLayout.LayoutParams(0, unusedAreaHeight.toInt())
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(id, topSide, unusedAreaView.id, topSide)
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaBottom.apply {
                    layoutParams = ConstraintLayout.LayoutParams(0, unusedAreaHeight.toInt())
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(id, bottomSide, unusedAreaView.id, bottomSide)
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaStart.apply {
                    layoutParams = ConstraintLayout.LayoutParams(unusedAreaWidth.toInt(), 0)
                    ConstraintSet().let {
                        it.clone(unusedAreaView)
                        it.connect(id, startSide, unusedAreaView.id, startSide)
                        it.connect(id, topSide, unusedAreaViewTop.id, bottomSide)
                        it.connect(id, bottomSide, unusedAreaViewBottom.id, topSide)
                        it.applyTo(unusedAreaView)
                    }
                }
                binding.adbCameralibraryViewUnusedAreaEnd.apply {
                    layoutParams = ConstraintLayout.LayoutParams(unusedAreaWidth.toInt(), 0)
                    ConstraintSet().also {
                        it.clone(unusedAreaView)
                        it.connect(id, endSide, unusedAreaView.id, endSide)
                        it.connect(id, topSide, unusedAreaViewTop.id, bottomSide)
                        it.connect(id, bottomSide, unusedAreaViewBottom.id, topSide)
                        it.applyTo(unusedAreaView)
                    }
                }
            }

            // 수평선.
            binding.adbCameralibraryViewHorizon.apply {
                layoutParams = ConstraintLayout.LayoutParams(1, 1)
                ConstraintSet().also {
                    it.clone(unusedAreaView)
                    it.connect(id, startSide, unusedAreaView.id, startSide)
                    it.connect(id, endSide, unusedAreaView.id, endSide)
                    it.connect(id, topSide, unusedAreaView.id, topSide)
                    it.connect(id, bottomSide, unusedAreaView.id, bottomSide)
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
                    ConstraintSet().also {
                        it.clone(unusedAreaView)
                        it.connect(id, startSide, unusedAreaView.id, startSide)
                        it.connect(id, endSide, unusedAreaView.id, endSide)
                        it.connect(id, topSide, unusedAreaView.id, topSide)
                        it.connect(id, bottomSide, unusedAreaView.id, bottomSide)
                        it.applyTo(unusedAreaView)
                    }
                    val innerTransition = ChangeBounds()
                    innerTransition.interpolator = AccelerateDecelerateInterpolator()
                    TransitionManager.beginDelayedTransition(
                        binding.adbCameralibraryLayout, innerTransition
                    )
                })
                TransitionManager.beginDelayedTransition(binding.adbCameralibraryLayout, transition)
            }
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases.
    private fun initCamera() {
        // Wait for the views to be properly laid out
        binding.adbCameralibraryPreviewView.post {
            // Setup for Camera UseCases.
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
        //
        imageProcessor?.stop()
        // Must unbind the use-cases before rebinding them
        cameraProvider?.unbindAll()

        // Preview UseCase.
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
            .build()
        preview?.setSurfaceProvider(binding.adbCameralibraryPreviewView.surfaceProvider)

        // ImageCapture UseCase.
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
            .setFlashMode(viewModel.flashMode)
            .build()

        // Processor for detector.
        imageProcessor = KoreanTextRecognizerOptions.Builder().build().let { processor ->
            when (viewModel.item.value.action) {
                IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES -> {
                    MileageRecognitionProcessor(requireContext(), processor)
                }
                IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES -> {
                    VinNumberRecognitionProcessor(requireContext(), processor)
                }
                IntentKey.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES -> {
                    VehicleNumberRecognitionProcessor(requireContext(), processor)
                }
                else -> null
            }
        }?.also { processor ->
            // Clear.
            imageAnalyzer?.clearAnalyzer()
            // Update overlay information.
            var needUpdateGraphicOverlayImageSourceInfo = true
            // ImageAnalysis UseCase.
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
                .build()
            imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        detectOverlay.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        detectOverlay.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }

                try {
                    imageProcessor?.processImageProxy(imageProxy, detectOverlay)
                } catch (ex: MlKitException) {
                    ex.printStackTrace()
                }
            }

            // 분석 완료.
            processor.onComplete { detectText, detectRect ->
                imageAnalyzer?.clearAnalyzer()
                imageProcessor?.run { this.stop() }
                when (viewModel.item.value.action) {
                    // 차량번호.
                    IntentKey.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES -> {
                        val analysisSize =
                            Size(detectOverlay.imageWidth, detectOverlay.imageHeight)
                        val scaleRect = viewModel.scaleRect(
                            detectRect,
                            analysisSize,
                            imageCapture?.resolutionInfo?.resolution ?: Size(0, 0)
                        )
                        takePicture(detectText, scaleRect)
                    }
                    // 그 외.
                    else -> {
                        viewModel.detectInImage(detectText, detectRect)
                    }
                }
            }
        }

        // Bind UseCase to Lifecycle.
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        camera = if (imageAnalyzer == null) {
            cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
        } else {
            cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
        }
    }

    // 후면 카메라 사용 가능.
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    // 전면 카메라 사용 가능.
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    // 셔터음.
    private fun playShutterSound(canMute: Boolean) {
        if (canMute) {
            // 미디어 볼륨으로 셔터효과음 재생(무음 가능).
            mediaActionSound.playWithStreamVolume(MediaActionSound.SHUTTER_CLICK, audioManager)
        } else {
            // 최소 볼륨으로 셔터효과음 재생.
            mediaActionSound.playWithMinimumVolume(MediaActionSound.SHUTTER_CLICK)
        }
    }

    // 이미지 가져오기.
    private fun takePicture(detectText: String? = null, detectRect: RectF? = null) {
        imageCapture?.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                // Debug.
                Timber.i(">>>>> ImageCapture onCaptureSuccess")

                val frameMetadata = FrameMetadata(image.width, image.height, image.imageInfo.rotationDegrees)
                imageProcessor?.processByteBuffer(image.planes[0].buffer, frameMetadata, detectOverlay)

//                // 이미지 저장.
//                try {
//                    viewModel.saveImage(
//                        image.planes[0].buffer,
//                        image.width,
//                        image.height,
//                        image.imageInfo.rotationDegrees,
//                        detectText,
//                        detectRect
//                    )
//                } catch (ex: IOException) {
//                    ex.printStackTrace()
//                }
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                // Debug.
                Timber.e(">>>>> OnImageSavedCallback onError: ${exception.message}")
            }
        })
    }
}