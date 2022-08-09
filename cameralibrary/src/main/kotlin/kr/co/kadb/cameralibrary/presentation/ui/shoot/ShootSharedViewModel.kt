package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.camera.core.ImageCapture
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.toThumbnail
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_TAKE_MULTIPLE_PICTURES
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * Created by oooobang on 2022. 7. 11..
 * ViewModel.
 */
internal class ShootSharedViewModel
constructor(
    application: Application,
    @Suppress("UNUSED_PARAMETER") preferences: PreferenceManager
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
        get() = PreferenceManager.getInstance(
            getApplication<Application>().applicationContext
        ).flashMode
        set(value) {
            PreferenceManager.getInstance(
                getApplication<Application>().applicationContext
            ).flashMode = value
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
        cropPercent: Array<Float>?,
        horizonColor: Int,
        unusedAreaBorderColor: Int
    ) {
        // Update.
        state.value.value?.copy(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            cropPercent = cropPercent?.toList() ?: listOf(),
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor,
            unusedAreaBorderColor = unusedAreaBorderColor
        ) ?: ShootUiState(
            action = action,
            isDebug = isDebug,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            cropPercent = cropPercent?.toList() ?: listOf(),
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = horizonColor,
            unusedAreaBorderColor = unusedAreaBorderColor
        ).run {
            updateState(isLoading = false, value = this)
        }
    }

    // Horizon, UnusedAreaBorder 색상.
    fun horizonAndUnusedAreaBorderColor(): Pair<Int, Int> {
        return Pair(item.value.horizonColor, item.value.unusedAreaBorderColor)
    }

    // 사용하지 않는 영역 크기.
    fun unusedAreaSize(rotation: Int, width: Int, height: Int): Pair<Float, Float> {
        return if (item.value.cropPercent.size == 2) {
            when (rotation) {
                0, 2 -> {
                    Pair(
                        (width.toFloat() * (1.0f - item.value.cropPercent[0]) * 0.5f),
                        (height.toFloat() * (1.0f - item.value.cropPercent[1]) * 0.5f)
                    )
                }
                else -> {
                    Pair(
                        (width.toFloat() * (1.0f - item.value.cropPercent[1]) * 0.5f),
                        (height.toFloat() * (1.0f - item.value.cropPercent[0]) * 0.5f)
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

    // 이미지 저장.
    @SuppressLint("RestrictedApi")
    fun saveImage(byteBuffer: ByteBuffer, width: Int, height: Int, rotation: Int) {
        // 셔터음 이벤트.
        event(Event.PlayShutterSound(item.value.canMute))

        // 이미지 저장.
        val context = getApplication<Application>().applicationContext
        val byteArray = ByteArray(byteBuffer.capacity()).also {
            byteBuffer.get(it)
        }

        // 이미지 Uri.
        val uri = byteArray.save(context, true)?.toUri() ?: Uri.EMPTY
        // 이미지 사이즈.
        val size = Size(width, height)

        // 상태 업데이트.
        updateState { value ->
            val uris = arrayListOf<Uri>().apply {
                addAll(value?.uris ?: arrayListOf())
                add(uri)
            }
            val sizes = arrayListOf<Size>().apply {
                addAll(value?.sizes ?: arrayListOf())
                add(size)
            }
            val rotations = arrayListOf<Int>().apply {
                addAll(value?.rotations ?: arrayListOf())
                add(rotation)
            }
            value?.copy(
                uris = uris,
                sizes = sizes,
                rotations = rotations
            )
        }

        // 촬영 완료.
        if (!item.value.isMultiplePicture) {
            // Thumbnail.
            val thumbnail = uri?.toThumbnail(context, size)
            // 촬영완료 이벤트.
            event(Event.TakePicture(uri, size, rotation, thumbnail))
        }
    }
}