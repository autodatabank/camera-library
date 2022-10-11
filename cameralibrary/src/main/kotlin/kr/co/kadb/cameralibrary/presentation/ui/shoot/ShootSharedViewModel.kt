package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.Size
import androidx.annotation.IntRange
import androidx.camera.core.ImageCapture
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.*
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_TAKE_MULTIPLE_PICTURES
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Created by oooobang on 2022. 7. 11..
 * ViewModel.
 */
internal class ShootSharedViewModel
constructor(
    application: Application,
    private val preferences: PreferenceManager
) : BaseAndroidViewModel<ShootUiState>(application, UiState.loading()) {
    // Event.
    sealed class Event {
        data class PlayShutterSound(val canMute: Boolean) : Event()
        data class TakePicture(
            val uri: Uri, val size: Size, val rotation: Int, val thumbnailBitmap: Bitmap?
        ) : Event()

        data class TakeMultiplePictures(
            val uris: ArrayList<Uri>, val sizes: ArrayList<Size>, val rotations: ArrayList<Int>
        ) : Event()

        data class DetectInImage(
            val text: String? = null,
            val rect: RectF? = null,
            val uri: Uri? = null,
            val size: Size? = null,
            val rotation: Int? = null,
            val thumbnailBitmap: Bitmap? = null
        ) : Event()
    }

    // Event.
    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Item.
    val item: StateFlow<ShootUiState> = state
        .map {
            it.getOrDefault(ShootUiState.Uninitialized)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShootUiState.Uninitialized)

    @Suppress("unused")
    val isEmpty: StateFlow<Boolean> = state.filter { !it.isLoading }
        .map {
            it.value?.action.isNullOrEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // 카메라 플래쉬 모드.
    var flashMode: Int = ImageCapture.FLASH_MODE_OFF
        get() = preferences.flashMode
        set(value) {
            preferences.flashMode = value
            field = value
        }

    init {
        // UIState.
        viewModelScope.launch {
            item.collect { item ->
                // Debug.
                Timber.i(">>>>> ShootSharedViewModel item : %s", item)
            }
        }
    }

    // Emit event.
    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    // Intent Action 설정.
    fun initUiState(
        action: String?,
        isDebug: Boolean = false,
        canMute: Boolean = false,
        hasHorizon: Boolean = false,
        canUiRotation: Boolean = false,
        isSaveCroppedImage: Boolean = false,
        cropSize: CropSize?,
        horizonColor: Int,
        unusedAreaBorderColor: Int,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int
    ) {
        val isUsingMLKit = action == ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
                || action == ACTION_DETECT_MILEAGE_IN_PICTURES
                || action == ACTION_DETECT_VIN_NUMBER_IN_PICTURES
        val isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES
        val isMileagePicture = action == ACTION_DETECT_MILEAGE_IN_PICTURES
        val isVinNumberPicture = action == ACTION_DETECT_VIN_NUMBER_IN_PICTURES
        val isVehicleNumberPicture = action == ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
        val canSaveCroppedImage = if (isUsingMLKit) {
            false
        } else {
            isSaveCroppedImage && cropSize?.isNotEmpty == true
        }
        val imageCropSize: CropSize = if (isUsingMLKit) {
            CropSize.Uninitialized
        } else {
            cropSize ?: CropSize.Uninitialized
        }

        // Update.
        state.value.value?.copy(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = isMultiplePicture,
            isUsingMLKit = isUsingMLKit,
            isVehicleNumberPicture = isVehicleNumberPicture,
            isMileagePicture = isMileagePicture,
            isVinNumberPicture = isVinNumberPicture,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            isSaveCroppedImage = canSaveCroppedImage,
            cropSize = imageCropSize,
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor,
            unusedAreaBorderColor = unusedAreaBorderColor,
            croppedJpegQuality = croppedJpegQuality
        ) ?: ShootUiState(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = isMultiplePicture,
            isUsingMLKit = isUsingMLKit,
            isVehicleNumberPicture = isVehicleNumberPicture,
            isMileagePicture = isMileagePicture,
            isVinNumberPicture = isVinNumberPicture,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            isSaveCroppedImage = canSaveCroppedImage,
            cropSize = imageCropSize,
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor,
            unusedAreaBorderColor = unusedAreaBorderColor,
            croppedJpegQuality = croppedJpegQuality
        ).run {
            // Debug.
            Timber.d(">>>>> ShootUiState : ${this.toJsonPretty()}")
            // Update.
            updateState(isLoading = false, value = this)
        }
    }
//
//    // Horizon, UnusedAreaBorder 색상.
//    fun horizonAndUnusedAreaBorderColor(): Pair<Int, Int> {
//        return Pair(item.value.horizonColor, item.value.unusedAreaBorderColor)
//    }

    // 사용하지 않는 영역 크기.
    fun unusedAreaSize(rotation: Int, width: Int, height: Int): Pair<Float, Float> {
        return if (item.value.cropSize.isNotEmpty) {
            when (rotation) {
                0, 2 -> {
                    Pair(
                        (width.toFloat() * (1.0f - item.value.cropSize.width) * 0.5f),
                        (height.toFloat() * (1.0f - item.value.cropSize.height) * 0.5f)
                    )
                }
                else -> {
                    Pair(
                        (width.toFloat() * (1.0f - item.value.cropSize.height) * 0.5f),
                        (height.toFloat() * (1.0f - item.value.cropSize.width) * 0.5f)
                    )
                }
            }
        } else {
            Pair(0.0f, 0.0f)
        }
    }

    // 촬영 가능 여부.
    fun canTakePicture(): Boolean {
        return item.value.let {
            if (it.isShooted && !it.isMultiplePicture) {
                false
            } else {
                updateState { value ->
                    value?.copy(isShooted = true)
                }
                true
            }
        }
    }

    // 촬영완료 이벤트.
    fun stopShooting() {
        event(
            Event.TakeMultiplePictures(
                item.value.uris, item.value.sizes, item.value.rotations
            )
        )
    }

    // 이미지에서 텍스트 감지.
    fun detectInImage(text: String, rect: RectF) {
        event(Event.DetectInImage(text, rect))
    }

    // 이미지 저장.
    fun saveImage(
        byteBuffer: ByteBuffer,
        width: Int,
        height: Int,
        rotation: Int,
        detectText: String? = null,
        detectRect: RectF? = null
    ) {
        // 셔터음 이벤트.
        event(Event.PlayShutterSound(item.value.canMute))

        // Image Buffer
        byteBuffer.rewind()
        val byteArray = ByteArray(byteBuffer.capacity()).also {
            byteBuffer.get(it)
        }

        // 이미지 저장 및 상태 업데이트.
        viewModelScope.launch {
            imageSave(
                byteArray,
                item.value.isSaveCroppedImage,
                item.value.croppedJpegQuality
            ) { imagePath, imageUri, imageSize ->
                // 이미지 사이즈.
                val size = imageSize?.let {
                    Size(it.width, it.height)
                } ?: Size(width, height)

                // 상태 업데이트.
                updateState { value ->
                    val uris = arrayListOf<Uri>().apply {
                        addAll(value?.uris ?: arrayListOf())
                        add(imageUri ?: Uri.EMPTY)
                    }
                    val sizes = arrayListOf<Size>().apply {
                        addAll(value?.sizes ?: arrayListOf())
                        add(size)
                    }
                    val rotations = arrayListOf<Int>().apply {
                        addAll(value?.rotations ?: arrayListOf())
                        add(rotation)
                    }
                    value?.copy(uris = uris, sizes = sizes, rotations = rotations)
                }

                // 촬영 완료.
                if (item.value.isVehicleNumberPicture) {
                    // Thumbnail.
                    val context = getApplication<Application>().applicationContext
                    val thumbnail = imageUri?.toThumbnail(context, size)
                    // 감지 완료 이벤트.
                    event(
                        Event.DetectInImage(
                            detectText,
                            detectRect,
                            imageUri ?: Uri.EMPTY,
                            size,
                            rotation,
                            thumbnail
                        )
                    )
                } else if (!item.value.isMultiplePicture) {
                    // Thumbnail.
                    val context = getApplication<Application>().applicationContext
                    val thumbnail = imageUri?.toThumbnail(context, size)
                    // 촬영 완료 이벤트.
                    event(Event.TakePicture(imageUri ?: Uri.EMPTY, size, rotation, thumbnail))
                }
            }
        }
    }

    // 이미지 저장.
    private fun imageSave(
        byteArray: ByteArray,
        isSaveCroppedImage: Boolean,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int,
        action: (path: String?, uri: Uri?, size: Size?) -> Unit
    ) {
        val context = getApplication<Application>().applicationContext
        if (isSaveCroppedImage) {
            // Get the original ExifInterface.
            val inputStream: InputStream = ByteArrayInputStream(byteArray)
            var exifInterface: ExifInterface? = null
            try {
                exifInterface = ExifInterface(inputStream)
            } catch (ex: IOException) {
                ex.printStackTrace()
            } finally {
                inputStream.close()
            }
            // Croped Bitmap.
            val bitmap = byteArray.toBitmap()
            val cropBitmap = bitmap?.centerCrop(
                item.value.cropSize.width,
                item.value.cropSize.height,
                exifInterface?.rotationDegrees
            )

            // Save Bitmap.
            cropBitmap.save(
                context, true, exifInterface = exifInterface, jpegQuality = croppedJpegQuality
            ) { imagePath, imageUri ->
                action.invoke(
                    imagePath,
                    imageUri,
                    Size(cropBitmap?.width ?: 0, cropBitmap?.height ?: 0)
                )
            }
            bitmap?.recycle()
            cropBitmap?.recycle()
        } else {
            // Save Bitmap.
            byteArray.save(context, true) { imagePath, imageUri ->
                action.invoke(imagePath, imageUri, null)
            }
        }
    }

    // ImageSize에 맞게 조정 된 Detect Rect 반환.
    fun scaleRect(detectRect: RectF, analysisImageSize: Size, imageSize: Size): RectF {
        val maxImageWidth = max(imageSize.width, imageSize.height).toFloat()
        val maxAnalysisImageWidth = max(analysisImageSize.width, analysisImageSize.height).toFloat()
        val ratio = maxImageWidth.div(maxAnalysisImageWidth)
        val addition = 250.0f
        val scaleRect = RectF(
            detectRect.left * ratio - addition,
            detectRect.top * ratio - addition,
            detectRect.right * ratio + addition,
            detectRect.bottom * ratio + addition
        )
        val left = if (scaleRect.left < 0) 0.0f else scaleRect.left
        val top = if (scaleRect.top < 0) 0.0f else scaleRect.top
        val right = if (left + scaleRect.width() > imageSize.width) {
            imageSize.width.toFloat()
        } else {
            scaleRect.right
        }
        val bottom = if (top + scaleRect.height() > imageSize.height) {
            imageSize.height.toFloat()
        } else {
            scaleRect.bottom
        }
        return RectF(left, top, right, bottom)
    }
}