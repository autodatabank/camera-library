package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Bundle
import android.util.Size
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseViewBindingFragment
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootEvent.*
import kr.co.kadb.cameralibrary.presentation.widget.extension.repeatOnStarted
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.*
import kr.co.kadb.cameralibrary.presentation.widget.util.*
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Modified by oooobang on 2022. 7. 16..
 * Fragment for this app. Implements all camera operations including:
 * - Viewfinder
 * - Photo taking
 * - Image analysis
 */
internal class ShootFragment : BaseViewBindingFragment<AdbCameralibraryFragmentShootBinding>() {

    companion object {
        fun create() = ShootFragment()
    }

    // ViewBinding.
    override fun fragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        AdbCameralibraryFragmentShootBinding.inflate(inflater, container, false)

    // ViewModel.
    private val viewModel: ShootSharedViewModel by activityViewModels {
        ShootSharedViewModelFactory(requireContext())
        //ShootSharedViewModelFactory(CameraServiceLocator.getInstance(requireActivity().application))
    }

    private val portraitConstraint by lazy {
        ConstraintSet().apply {
            clone(context, R.layout.adb_cameralibrary_fragment_shoot_alt)
        }
    }

    private val landscapeConstraint by lazy {
        ConstraintSet().apply {
            clone(context, R.layout.adb_cameralibrary_fragment_shoot)
        }
    }

    // 이미지 분석 Overlay.
    private val detectOverlay by lazy {
        binding.adbCameralibraryGraphicOverlay
    }

    // AudioManager.
    private val audioManager: AudioManager? by lazy {
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
                if (!hasBinding || orientation == ORIENTATION_UNKNOWN) {
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
                    initLayoutAfterRotation()
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

    override fun onStart() {
        super.onStart()

        // 권한 확인 후 카메라 및 UI 초기화.
        viewController.requestCameraPermission {
            initCamera()
            initLayoutAfterRotation()
        }
    }

    override fun onResume() {
        super.onResume()

        // Enable orientation listener.
        orientationEventListener.enable()
    }

    override fun onPause() {
        super.onPause()

        // Disable orientation listener.
        orientationEventListener.disable()

        //
        imageProcessor?.run { this.stop() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        mediaActionSound.release()
    }

    override fun onBackPressed(): Boolean {
        viewModel.stopShooting()
        return false
    }

    // Init Layout.
    override fun initScreen(view: View, savedInstanceState: Bundle?) {
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
                when (event) {
                    // 셔터음.
                    is PlayShutterSound -> playShutterSound(event.canMute)
                    // 한 장 촬영 결과 전달.
                    is TakePicture -> Intent().apply {
                        action = viewModel.item.value.action
                        putExtra("data", event.thumbnailBitmap)
                        putExtra(IntentKey.EXTRA_WIDTH, event.size.width)
                        putExtra(IntentKey.EXTRA_HEIGHT, event.size.height)
                        putExtra(IntentKey.EXTRA_ROTATION, event.rotation)
                        setDataAndType(event.uri, "image/jpeg")
                    }.run {
                        requireActivity().setResult(Activity.RESULT_OK, this)
                        activity?.finish()
                        //event.thumbnailBitmap?.recycle()
                    }
                    // 여러 장 촬영 결과 전달.
                    is TakeMultiplePictures -> Intent().apply {
                        action = viewModel.item.value.action
                        putExtra(IntentKey.EXTRA_URIS, event.uris)
                        putExtra(IntentKey.EXTRA_SIZES, event.sizes)
                        putExtra(IntentKey.EXTRA_ROTATIONS, event.rotations)
                    }.run {
                        requireActivity().setResult(Activity.RESULT_OK, this)
                        activity?.finish()
                    }
                    // 이미지 감지 결과 전달.
                    is DetectInImage -> Intent().apply {
                        action = viewModel.item.value.action
                        putExtra(IntentKey.EXTRA_DETECT_TEXT, event.text)
                        putExtra(IntentKey.EXTRA_DETECT_RECT, event.rect)
                        putExtra("data", event.thumbnailBitmap)
                        putExtra(IntentKey.EXTRA_WIDTH, event.size?.width)
                        putExtra(IntentKey.EXTRA_HEIGHT, event.size?.height)
                        putExtra(IntentKey.EXTRA_ROTATION, event.rotation)
                        setDataAndType(event.uri, "image/jpeg")
                    }.run {
                        requireActivity().setResult(Activity.RESULT_OK, this)
                        activity?.finish()
                        //event.thumbnailBitmap?.recycle()
                    }
                }
            }
        }
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.adbCameralibraryButtonShooting.setOnClickListener {
            if (viewModel.canTakePicture()) {
                takePicture()
            }
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

    // Init layout.
    private fun initLayoutAfterRotation() {
        binding.adbCameralibraryPreviewView.post {
            // 여러장 촬영 상태에서만 촬영완료 버튼 활성화.
            val isMultiplePicture = viewModel.item.value.isMultiplePicture
            val isMileagePicture = viewModel.item.value.isMileagePicture
            val isVinNumberPicture = viewModel.item.value.isVinNumberPicture
            val isMultipleVisibility = when (isMultiplePicture) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            val isShootVisibility = when (isMileagePicture || isVinNumberPicture) {
                true -> View.GONE
                else -> View.VISIBLE
            }

            // rotation.
            val targetRotation = imageCapture?.targetRotation ?: 0

            // 플래쉬, 촬영, 완료 버튼 회전.
            with((targetRotation * 90).toFloat()) {
                binding.adbCameralibraryLayoutFlash.animate().rotation(this)
                binding.adbCameralibraryLayoutFinish.animate().rotation(this)
                binding.adbCameralibraryButtonShooting.animate().rotation(this)
            }

            // 수평선 회전.
            val layoutParams = ConstraintLayout.LayoutParams(1, 1).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
            binding.adbCameralibraryViewHorizon.layoutParams = layoutParams
            binding.adbCameralibraryViewHorizon.requestLayout()
            binding.adbCameralibraryViewHorizon.post {
                when (targetRotation) {
                    1, 3 -> {
                        portraitConstraint.apply {
                            setVisibility(
                                binding.adbCameralibraryLayoutFinish.id, isMultipleVisibility
                            )
                            setVisibility(
                                binding.adbCameralibraryLayoutFlash.id, isShootVisibility
                            )
                            setVisibility(
                                binding.adbCameralibraryButtonShooting.id, isShootVisibility
                            )
                        }
                        portraitConstraint.applyTo(binding.root)
                        TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
                    }

                    else -> {
                        landscapeConstraint.apply {
                            setVisibility(
                                binding.adbCameralibraryLayoutFinish.id, isMultipleVisibility
                            )
                            setVisibility(
                                binding.adbCameralibraryLayoutFlash.id, isShootVisibility
                            )
                            setVisibility(
                                binding.adbCameralibraryButtonShooting.id, isShootVisibility
                            )
                        }
                        landscapeConstraint.applyTo(binding.root)
                        TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
                    }
                }
            }
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases.
    private fun initCamera() {
        // Wait for the views to be properly laid out
        binding.adbCameralibraryPreviewView.post {
            // Setup for Camera UseCases.
            ProcessCameraProvider.getInstance(requireContext()).apply {
                addListener({
                    // CameraProvider
                    cameraProvider = get()
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
    }

    private fun bindCameraUseCases() {
        // 이미지 처리기가 있다면 중지시킵니다.
        imageProcessor?.stop()
        // 기존에 바인드된 use-cases를 해제합니다.
        cameraProvider?.unbindAll()

        val imageAnalysisSize = Size(1024, 768)
        //val imageAnalysisSize = Size(800, 600)
        //val imageAnalysisSize = Size(1024, 768)
        //val imageAnalysisSize = Size(1280, 720)
        val imageAnalysisResolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    imageAnalysisSize,
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            ).build()

        // Preview UseCase를 설정합니다.
        preview = Preview.Builder()
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
            //.setResolutionSelector(resolutionSelector)
            .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
            .build()
        preview?.setSurfaceProvider(binding.adbCameralibraryPreviewView.surfaceProvider)

        // ImageCapture UseCase를 설정합니다.
        imageCapture = ImageCapture.Builder()
            //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
//            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
            .setFlashMode(viewModel.flashMode)
            .build()

        // 프로세서를 초기화합니다.
        imageProcessor = when (viewModel.item.value.action) {
            ACTION_DETECT_MILEAGE_IN_PICTURES -> MileageRecognitionProcessor(
                requireContext(), createKoreanTextRecognizerOptions()
            )

            ACTION_DETECT_VIN_NUMBER_IN_PICTURES -> VinNumberRecognitionProcessor(
                requireContext(), createKoreanTextRecognizerOptions()
            )

            ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES -> VehicleNumberRecognitionProcessor(
                requireContext(), createKoreanTextRecognizerOptions()
            )

            else -> null
        }?.also { processor ->
            //imageProcessor?.let { processor ->
            // clearAnalyzer를 호출하기 전에 analyzer를 초기화합니다.
            imageAnalyzer?.clearAnalyzer()

            // 그래픽 오버레이 이미지 소스 정보 업데이트 필요.
            needUpdateGraphicOverlayImageSourceInfo = true

            // ImageAnalysis UseCase를 설정합니다.
            imageAnalyzer = ImageAnalysis.Builder()
                //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setResolutionSelector(imageAnalysisResolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(binding.adbCameralibraryPreviewView.display.rotation)
                .build()
            imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
                updateOverlayImageSourceInfoIfNeeded(imageProxy)
                processImageProxySafely(imageProxy)
            }

            // Processor를 사용하여 onComplete를 호출합니다.
            processor.onComplete { detectText, detectRect ->
                handleCompleteAction(detectText, detectRect)
            }
        }

        // CameraSelector를 설정하고 UseCase를 Lifecycle에 바인드합니다.
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        camera = when (imageAnalyzer == null) {
            true -> cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )

            else -> cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
        }
    }

    // Overlay의 이미지 소스 정보를 업데이트합니다.
    private var needUpdateGraphicOverlayImageSourceInfo = true
    private fun updateOverlayImageSourceInfoIfNeeded(imageProxy: ImageProxy) {
        if (needUpdateGraphicOverlayImageSourceInfo) {
            val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val (width, height) = when (rotationDegrees == 0 || rotationDegrees == 180) {
                true -> Pair(imageProxy.width, imageProxy.height)
                else -> Pair(imageProxy.height, imageProxy.width)
            }
            detectOverlay.setImageSourceInfo(width, height, isImageFlipped)
            needUpdateGraphicOverlayImageSourceInfo = false
        }
    }

    // 이미지 프록시를 안전하게 처리합니다.
    private fun processImageProxySafely(imageProxy: ImageProxy) {
        try {
            imageProcessor?.processImageProxy(imageProxy, detectOverlay)
        } catch (ex: MlKitException) {
            ex.printStackTrace()
        }/* finally {
            imageProxy.close()
        }*/
    }

    // Processor 옵션을 생성합니다.
    private fun createKoreanTextRecognizerOptions(): KoreanTextRecognizerOptions {
        return KoreanTextRecognizerOptions
            .Builder()
            .build()
    }

    // 처리 완료 후 액션을 처리합니다.
    private fun handleCompleteAction(detectText: String, detectRect: RectF) {
        imageAnalyzer?.clearAnalyzer()
        imageProcessor?.stop()
        /*when (viewModel.item.value.action) {
            ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES -> {
                val scaleRect = viewModel.scaleRect(
                    detectRect,
                    Size(detectOverlay.imageWidth, detectOverlay.imageHeight),
                    imageCapture?.resolutionInfo?.resolution ?: Size(0, 0)
                )
                takePicture(detectText, scaleRect)
            }

            else -> viewModel.detectInImage(detectText, detectRect)
        }*/
        viewModel.detectInImage(detectText, detectRect)
    }

    // 후면 카메라 사용 가능.
    private fun hasBackCamera(): Boolean = cameraProvider?.hasCamera(
        CameraSelector.DEFAULT_BACK_CAMERA
    ) ?: false

    // 전면 카메라 사용 가능.
    private fun hasFrontCamera(): Boolean = cameraProvider?.hasCamera(
        CameraSelector.DEFAULT_FRONT_CAMERA
    ) ?: false

    // 셔터음.
    private fun playShutterSound(canMute: Boolean) {
        when (canMute) {
            // 미디어 볼륨으로 셔터효과음 재생(무음 가능).
            true -> mediaActionSound.playWithStreamVolume(
                MediaActionSound.SHUTTER_CLICK, audioManager
            )
            // 최소 볼륨으로 셔터효과음 재생.
            else -> mediaActionSound.playWithMinimumVolume(
                MediaActionSound.SHUTTER_CLICK
            )
        }
    }

    // 이미지 가져오기.
    private fun takePicture(detectText: String? = null, detectRect: RectF? = null) {
        imageCapture?.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                // 이미지 저장.
                try {
                    viewModel.saveImage(
                        image.planes[0].buffer,
                        image.width,
                        image.height,
                        image.imageInfo.rotationDegrees,
                        detectText,
                        detectRect
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                // Debug.
                DebugLog.e { "OnImageSavedCallback onError: ${exception.message}" }
            }
        })
    }
}