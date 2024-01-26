package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.RectF
import android.net.Uri
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.base.UiState
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootEvent.*
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.*
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_MAINTENANCE_STATEMENT_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_TAKE_MULTIPLE_PICTURES
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Created by oooobang on 2022. 7. 11..
 * ViewModel.
 */
internal class ShootSharedViewModel(
    private val application: Application,
    //private val saveImageUseCase: SaveImageUseCase,
    private val preferences: PreferenceManager
) : BaseViewModel<ShootUiState>(UiState.loading()) {

    // Event.
    private val _eventFlow = MutableSharedFlow<ShootEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Item.
    val item: StateFlow<ShootUiState> = uiState
        .map {
            it.getOrDefault(ShootUiState.Uninitialized)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ShootUiState.Uninitialized)

    @Suppress("unused")
    val isEmpty: StateFlow<Boolean> = uiState.filter { !it.isLoading }
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
                DebugLog.i { ">>>>> ShootSharedViewModel item : $item" }
            }
        }
    }

    // Emit event.
    private fun event(event: ShootEvent) {
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
        horizonColor: Int
    ) {
        val isUsingMLKit = action == ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
                || action == ACTION_DETECT_MILEAGE_IN_PICTURES
                || action == ACTION_DETECT_VIN_NUMBER_IN_PICTURES
                || action == ACTION_DETECT_MAINTENANCE_STATEMENT_IN_PICTURES
        val isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES
        val isMileagePicture = action == ACTION_DETECT_MILEAGE_IN_PICTURES
        val isVinNumberPicture = action == ACTION_DETECT_VIN_NUMBER_IN_PICTURES
        val isVehicleNumberPicture = action == ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES
        val isMaintenanceStatementPicture =
            action == ACTION_DETECT_MAINTENANCE_STATEMENT_IN_PICTURES

        // Update.
        uiState.value.value?.copy(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = isMultiplePicture,
            isUsingMLKit = isUsingMLKit,
            isVehicleNumberPicture = isVehicleNumberPicture,
            isMileagePicture = isMileagePicture,
            isVinNumberPicture = isVinNumberPicture,
            isMaintenanceStatementPicture = isMaintenanceStatementPicture,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor
        ) ?: ShootUiState(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = isMultiplePicture,
            isUsingMLKit = isUsingMLKit,
            isVehicleNumberPicture = isVehicleNumberPicture,
            isMileagePicture = isMileagePicture,
            isVinNumberPicture = isVinNumberPicture,
            isMaintenanceStatementPicture = isMaintenanceStatementPicture,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor
        ).run {
            // Debug.
            DebugLog.d { "ShootUiState : ${this.toJsonPretty()}" }
            // Update.
            updateState(isLoading = false, value = this)
        }
    }

    // 촬영 가능 여부.
    fun canTakePicture(): Boolean {
        return item.value.let {
            if (it.isShooted && !it.isMultiplePicture) {
                false
            } else {
                updateState { value ->
                    value.copy(isShooted = true)
                }
                true
            }
        }
    }

    // 촬영완료 이벤트.
    fun stopShooting() {
        event(
            TakeMultiplePictures(
                item.value.uris, item.value.sizes, item.value.rotations
            )
        )
    }

    // 이미지에서 텍스트 감지.
    fun detectInImage(text: String, rect: RectF) {
        event(DetectInImage(text, rect))
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
        event(PlayShutterSound(item.value.canMute))

        // Image Buffer
        byteBuffer.rewind()
        val byteArray = ByteArray(byteBuffer.capacity()).also {
            byteBuffer.get(it)
        }

        // 이미지 저장 및 상태 업데이트.
        viewModelScope.launch {
            imageSave(byteArray) { imagePath, imageUri, imageSize ->
                // 이미지 사이즈.
                val size = imageSize?.let {
                    Size(it.width, it.height)
                } ?: Size(width, height)

                // 상태 업데이트.
                updateState { value ->
                    val uris = arrayListOf<Uri>().apply {
                        addAll(value.uris)
                        add(imageUri ?: Uri.EMPTY)
                    }
                    val sizes = arrayListOf<Size>().apply {
                        addAll(value.sizes)
                        add(size)
                    }
                    val rotations = arrayListOf<Int>().apply {
                        addAll(value.rotations)
                        add(rotation)
                    }
                    value.copy(uris = uris, sizes = sizes, rotations = rotations)
                }

                // 촬영 완료.
                if (item.value.isVehicleNumberPicture) {
                    // Thumbnail.
                    val context = application.applicationContext
                    val thumbnail = imageUri?.toThumbnail(context, size)
                    // 감지 완료 이벤트.
                    event(
                        DetectInImage(
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
                    val context = application.applicationContext
                    val thumbnail = imageUri?.toThumbnail(context, size)
                    // 촬영 완료 이벤트.
                    event(TakePicture(imageUri ?: Uri.EMPTY, size, rotation, thumbnail))
                }
            }
        }
    }

    // 이미지 저장.
    private fun imageSave(
        byteArray: ByteArray, action: (path: String?, uri: Uri?, size: Size?) -> Unit
    ) {
        val context = application.applicationContext
        // Save Bitmap.
        byteArray.saveImage(context, true) { savedPath ->
            action.invoke(savedPath, savedPath?.toUri(), null)
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