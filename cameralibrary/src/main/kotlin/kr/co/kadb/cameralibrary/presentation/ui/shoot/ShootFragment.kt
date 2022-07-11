package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.MediaActionSound
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.databinding.AdbCameralibraryFragmentShootBinding
import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
import kr.co.kadb.cameralibrary.presentation.widget.util.MediaActionSound2
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by oooobang on 2020. 2. 3..
 * 촬영.
 */

/** Helper type alias used for analysis use case callbacks */
internal typealias LumaListener = (luma: Double) -> Unit

@AndroidEntryPoint
internal class ShootFragment : BaseBindingFragment<AdbCameralibraryFragmentShootBinding, ShootViewModel>() {
    companion object {
        fun create(extraTo: String?): ShootFragment {
            val fragment = ShootFragment()
            val bundle = Bundle()
            //bundle.putString(EXTRA_TO, extraTo)
            fragment.arguments = bundle
            return fragment
        }


        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
//
//        /** Helper function used to create a timestamped file */
//        private fun createFile(baseFolder: File, format: String, extension: String) =
//            File(
//                baseFolder, SimpleDateFormat(format, Locale.US)
//                    .format(System.currentTimeMillis()) + extension
//            )
    }

    @Inject
    lateinit var preferences: PreferenceManager

    @Inject
    lateinit var viewController: ShootController

    // ViewModel.
    override val viewModel: ShootViewModel by viewModels()

    private lateinit var windowManager: WindowManager

    //
    private var extraTo: String? = null

    // Fragment Layout.
    override val layoutResourceId: Int = R.layout.adb_cameralibrary_fragment_shoot


    private lateinit var mediaActionSound: MediaActionSound2 /*by lazy {
        MediaActionSound().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }*/

    val audioManager by lazy {
        context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        // Binding.
//        binding.viewModel = viewModel
//
//        // 이미지 패스.
//        extraTo = arguments?.getString(EXTRA_TO)
//
//        // CameraView Lifecycle Owner.
//        binding.cameraview.setLifecycleOwner(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()

        viewController.requestCameraPermission {
            // Granted.
            Timber.i(">>>>> requestCameraPermission Granted")

            initCamera()
        }
    }

    override fun onDestroyView() {
//        _fragmentCameraBinding = null
        super.onDestroyView()

        //
        mediaActionSound.release()

        // Shut down our background executor
        cameraExecutor.shutdown()
//
//        // Unregister the broadcast receivers and listeners
//        broadcastManager.unregisterReceiver(volumeDownReceiver)
//        displayManager.unregisterDisplayListener(displayListener)
    }


//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun initVariable() {
        mediaActionSound = MediaActionSound2().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }

    // Init Layout.
    override fun initLayout() {

        // Debug.
        Timber.i(">>>>> initLayout!!!!!")

//        // 차량등록증 촬영 시 선택 버튼 비활성.
//        if (extraTo == TO_SHOOTING_REGISTRATION_CARD) {
//            binding.buttonSelect.isVisible = false
//        }
//
//        // 카메라 로깅.
//        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
//
//        // 플래쉬.
//        when (preferences.cameraFlash) {
//            Flash.ON.ordinal -> {
//                binding.cameraview.flash = Flash.ON
//                binding.buttonFlash.setImageResource(R.drawable.baseline_flash_on_white_48)
//            }
//            Flash.AUTO.ordinal -> {
//                binding.cameraview.flash = Flash.AUTO
//                binding.buttonFlash.setImageResource(R.drawable.baseline_flash_auto_white_48)
//            }
//            else -> {
//                binding.cameraview.flash = Flash.OFF
//                binding.buttonFlash.setImageResource(R.drawable.baseline_flash_off_white_48)
//            }
//        }
    }

    // Init Observer.
    override fun initObserver() {
    }

    // Init Listener.
    override fun initListener() {
//        // 플래쉬.
//        binding.buttonFlash.setOnClickListener {
//            when (preferences.cameraFlash) {
//                Flash.ON.ordinal -> {
//                    preferences.cameraFlash = Flash.AUTO.ordinal
//                    binding.cameraview.flash = Flash.AUTO
//                    binding.buttonFlash.setImageResource(R.drawable.baseline_flash_auto_white_48)
//                }
//                Flash.AUTO.ordinal -> {
//                    preferences.cameraFlash = Flash.OFF.ordinal
//                    binding.cameraview.flash = Flash.OFF
//                    binding.buttonFlash.setImageResource(R.drawable.baseline_flash_off_white_48)
//                }
//                else -> {
//                    preferences.cameraFlash = Flash.ON.ordinal
//                    binding.cameraview.flash = Flash.ON
//                    binding.buttonFlash.setImageResource(R.drawable.baseline_flash_on_white_48)
//                }
//            }
//        }

        // 촬영.
        binding.buttonShooting.setOnClickListener {
            mediaActionSound.playWithStreamVolume(
                MediaActionSound.SHUTTER_CLICK,
                audioManager

            )
            //mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
//            MediaActionSound().apply {
//                (context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.let {
//                    this.play(MediaActionSound.SHUTTER_CLICK)
//                }
//            }

//            Timber.i(">>>>> FILE PATH : %s", context?.createFile(true))
//            Timber.i(">>>>> FILE PATH : %s", context?.createFile(false))
            // Get a stable reference of the modifiable image capture use case
            imageCapture?.let { imageCapture ->

                // Create output file to hold the image

//                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
//                val photoFile = context?.createFile(true)!!

                // Setup image capture metadata
                val metadata = Metadata().apply {

                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                // Create output options object which contains file + metadata
                val outputOptions = context?.outputFileOptionsBuilder(true)
                    ?.setMetadata(metadata)
                    ?.build()!!
//                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
//                    .setMetadata(metadata)
//                    .build()

                // Setup image capture listener which is triggered after photo has been taken
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Timber.e(">>>>> Photo capture failed: ${exc.message}", exc)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            Timber.i(">>>>> Photo capture succeeded: ${output.savedUri}")
//
//                            // We can only change the foreground Drawable using API level 23+ API
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                // Update the gallery thumbnail with latest picture taken
//                                setGalleryThumbnail(savedUri)
//                            }
//
//                            // Implicit broadcasts will be ignored for devices running API level >= 24
//                            // so if you only target API level 24+ you can remove this statement
//                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                                requireActivity().sendBroadcast(
//                                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, output.savedUri)
//                                )
//                            }
//
//                            // If the folder selected is an external media directory, this is
//                            // unnecessary but otherwise other apps will not be able to access our
//                            // images unless we scan them using [MediaScannerConnection]
//                            val mimeType = MimeTypeMap.getSingleton()
//                                .getMimeTypeFromExtension(output.savedUri?.toFile()?.extension)
//                            MediaScannerConnection.scanFile(
//                                context,
//                                arrayOf(output.savedUri?.toFile()?.absolutePath),
//                                arrayOf(mimeType)
//                            ) { _, uri ->
//                                Timber.d(">>>>> Image capture scanned into media store: $uri")
//                            }
                        }
                    })

//                // We can only change the foreground Drawable using API level 23+ API
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                    // Display flash animation to indicate that photo was captured
//                    fragmentCameraBinding.root.postDelayed({
//                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
//                        fragmentCameraBinding.root.postDelayed(
//                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS
//                        )
//                    }, ANIMATION_SLOW_MILLIS)
//                }
            }
        }
//
//        // 선택.
//        binding.buttonSelect.setOnClickListener {
//            viewController.navigateToShootingMany(viewModel.paths)
//        }
    }

    // Init Callback.
    override fun initCallback() {
//        // 촬영 후 결과.
//        binding.cameraview.onPictureTaken { result ->
//            viewModel.saveWithByte(result.data) {
//                // 차량등록증 촬영 시 사진 결과 화면으로...
//                if (extraTo == TO_SHOOTING_REGISTRATION_CARD) {
//                    viewController.navigateToShootingOne(viewModel.paths)
//                }
//            }
//        }
    }


//    private var _fragmentCameraBinding: FragmentCameraBinding? = null
//
//    private val fragmentCameraBinding get() = _fragmentCameraBinding!!
//
//    private var cameraUiContainerBinding: CameraUiContainerBinding? = null

//    private lateinit var outputDirectory: File
//    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
//    private lateinit var windowManager: WindowManager

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
//
//    /** Volume down button receiver used to trigger shutter */
//    private val volumeDownReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
//                // When the volume down button is pressed, simulate a shutter button click
//                KeyEvent.KEYCODE_VOLUME_DOWN -> {
//                    cameraUiContainerBinding?.cameraCaptureButton?.simulateClick()
//                }
//            }
//        }
//    }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@ShootFragment.displayId) {
                // Debug.
                Timber.d(">>>>> Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private fun initCamera() {
//
//
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        broadcastManager = LocalBroadcastManager.getInstance(view.context)
//
//        // Set up the intent filter that will receive events from our main activity
//        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
//        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        // Every time the orientation of device changes, update rotation for use cases
        displayManager.registerDisplayListener(displayListener, null)

        //Initialize WindowManager to retrieve display metrics
        windowManager =
            activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager // WindowManager(view?.context)
//
//        // Determine the output directory
//        outputDirectory = MainActivity.getOutputDirectory(requireContext())

        // Wait for the views to be properly laid out
        binding.previewView.post {

            // Keep track of the display in which this view is attached
            displayId = binding.previewView.display.displayId

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
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

            // Enable or disable switching between cameras
            updateCameraSwitchButton()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = windowManager.currentWindowMetrics.bounds
        // Debug.
        Timber.d(">>>>> Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        // Debug.
        Timber.d(">>>>> Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.previewView.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    // Debug.
                    //Timber.d(">>>>> Average luminosity: $luma")
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
            observeCameraState(camera?.cameraInfo!!)
        } catch (exc: Exception) {
            // Debug.
            Timber.e(">>>>> Use case binding failed: $exc")
        }
    }

    private fun observeCameraState(cameraInfo: CameraInfo) {
        cameraInfo.cameraState.observe(viewLifecycleOwner) { cameraState ->
            run {
                when (cameraState.type) {
                    CameraState.Type.PENDING_OPEN -> {
                        // Ask the user to close other camera apps
                        Toast.makeText(
                            context,
                            "CameraState: Pending Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPENING -> {
                        // Show the Camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Opening",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.OPEN -> {
                        // Setup Camera resources and begin processing
                        Toast.makeText(
                            context,
                            "CameraState: Open",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSING -> {
                        // Close camera UI
                        Toast.makeText(
                            context,
                            "CameraState: Closing",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.Type.CLOSED -> {
                        // Free camera resources
                        Toast.makeText(
                            context,
                            "CameraState: Closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            cameraState.error?.let { error ->
                when (error.code) {
                    // Open errors
                    CameraState.ERROR_STREAM_CONFIG -> {
                        // Make sure to setup the use cases properly
                        Toast.makeText(
                            context,
                            "Stream config error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Opening errors
                    CameraState.ERROR_CAMERA_IN_USE -> {
                        // Close the camera or ask user to close another camera app that's using the
                        // camera
                        Toast.makeText(
                            context,
                            "Camera in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_MAX_CAMERAS_IN_USE -> {
                        // Close another open camera in the app, or ask the user to close another
                        // camera app that's using the camera
                        Toast.makeText(
                            context,
                            "Max cameras in use",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> {
                        Toast.makeText(
                            context,
                            "Other recoverable error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closing errors
                    CameraState.ERROR_CAMERA_DISABLED -> {
                        // Ask the user to enable the device's cameras
                        Toast.makeText(
                            context,
                            "Camera disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    CameraState.ERROR_CAMERA_FATAL_ERROR -> {
                        // Ask the user to reboot the device to restore camera function
                        Toast.makeText(
                            context,
                            "Fatal error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Closed errors
                    CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> {
                        // Ask the user to disable the "Do Not Disturb" mode, then reopen the camera
                        Toast.makeText(
                            context,
                            "Do not disturb mode enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
//
//        // Remove previous UI if any
//        cameraUiContainerBinding?.root?.let {
//            fragmentCameraBinding.root.removeView(it)
//        }
//
//        cameraUiContainerBinding = CameraUiContainerBinding.inflate(
//            LayoutInflater.from(requireContext()),
//            fragmentCameraBinding.root,
//            true
//        )

        // In the background, load latest photo taken (if any) for gallery thumbnail
        lifecycleScope.launch(Dispatchers.IO) {
//            outputDirectory.listFiles { file ->
//                EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
//            }?.maxOrNull()?.let {
//                setGalleryThumbnail(Uri.fromFile(it))
//            }
        }
//
//        // Listener for button used to capture photo
//        cameraUiContainerBinding?.cameraCaptureButton?.setOnClickListener {
//
//            // Get a stable reference of the modifiable image capture use case
//            imageCapture?.let { imageCapture ->
//
//                // Create output file to hold the image
//                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
//
//                // Setup image capture metadata
//                val metadata = Metadata().apply {
//
//                    // Mirror image when using the front camera
//                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
//                }
//
//                // Create output options object which contains file + metadata
//                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
//                    .setMetadata(metadata)
//                    .build()
//
//                // Setup image capture listener which is triggered after photo has been taken
//                imageCapture.takePicture(
//                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
//                        override fun onError(exc: ImageCaptureException) {
//                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                        }
//
//                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
//                            Log.d(TAG, "Photo capture succeeded: $savedUri")
//
//                            // We can only change the foreground Drawable using API level 23+ API
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                // Update the gallery thumbnail with latest picture taken
//                                setGalleryThumbnail(savedUri)
//                            }
//
//                            // Implicit broadcasts will be ignored for devices running API level >= 24
//                            // so if you only target API level 24+ you can remove this statement
//                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                                requireActivity().sendBroadcast(
//                                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
//                                )
//                            }
//
//                            // If the folder selected is an external media directory, this is
//                            // unnecessary but otherwise other apps will not be able to access our
//                            // images unless we scan them using [MediaScannerConnection]
//                            val mimeType = MimeTypeMap.getSingleton()
//                                .getMimeTypeFromExtension(savedUri.toFile().extension)
//                            MediaScannerConnection.scanFile(
//                                context,
//                                arrayOf(savedUri.toFile().absolutePath),
//                                arrayOf(mimeType)
//                            ) { _, uri ->
//                                Log.d(TAG, "Image capture scanned into media store: $uri")
//                            }
//                        }
//                    })
//
//                // We can only change the foreground Drawable using API level 23+ API
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                    // Display flash animation to indicate that photo was captured
//                    fragmentCameraBinding.root.postDelayed({
//                        fragmentCameraBinding.root.foreground = ColorDrawable(Color.WHITE)
//                        fragmentCameraBinding.root.postDelayed(
//                            { fragmentCameraBinding.root.foreground = null }, ANIMATION_FAST_MILLIS
//                        )
//                    }, ANIMATION_SLOW_MILLIS)
//                }
//            }
//        }
//
//        // Setup for button used to switch cameras
//        cameraUiContainerBinding?.cameraSwitchButton?.let {
//
//            // Disable the button until the camera is set up
//            it.isEnabled = false
//
//            // Listener for button used to switch cameras. Only called if the button is enabled
//            it.setOnClickListener {
//                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
//                    CameraSelector.LENS_FACING_BACK
//                } else {
//                    CameraSelector.LENS_FACING_FRONT
//                }
//                // Re-bind use cases to update selected camera
//                bindCameraUseCases()
//            }
//        }
//
//        // Listener for button used to view the most recent photo
//        cameraUiContainerBinding?.photoViewButton?.setOnClickListener {
//            // Only navigate when the gallery has photos
//            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
//                Navigation.findNavController(
//                    requireActivity(), R.id.fragment_container
//                ).navigate(
//                    CameraFragmentDirections
//                        .actionCameraToGallery(outputDirectory.absolutePath)
//                )
//            }
//        }
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
//        try {
//            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = hasBackCamera() && hasFrontCamera()
//        } catch (exception: CameraInfoUnavailableException) {
//            cameraUiContainerBinding?.cameraSwitchButton?.isEnabled = false
//        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
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