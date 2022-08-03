package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.*
import kr.co.kadb.cameralibrary.presentation.widget.extension.exif
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.toJsonPretty
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
        data class TakePicture(val uri: Uri, val size: Size, val thumbnailBitmap: Bitmap?) : Event()
        data class TakeMultiplePictures(val uris: ArrayList<Uri>, val sizes: ArrayList<Size>) : Event()
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

    val isEmpty: StateFlow<Boolean> = state.filter { !it.isLoading }
        .map {
            it.value?.action.isNullOrEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
        canMute: Boolean = false,
        hasHorizon: Boolean = false,
        canUiRotation: Boolean = false,
        cropPercent: Array<Float>?
    ) {
        // Debug.
        Timber.i(">>>>> initUiState action : $action")
        Timber.i(">>>>> initUiState canMute : $canMute")
        Timber.i(">>>>> initUiState canUiRotation : $canUiRotation")
        Timber.i(">>>>> initUiState cropPercent : ${cropPercent.toJsonPretty()}")

        // Update.
        state.value.value?.copy(
            action = action,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            cropPercent = cropPercent?.toList() ?: listOf()
        ) ?: ShootUiState(
            action = action,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES,
            canMute = canMute,
            hasHorizon = hasHorizon,
            canUiRotation = canUiRotation,
            cropPercent = cropPercent?.toList() ?: listOf(),
            uris = arrayListOf(),
            sizes = arrayListOf()
        ).run {
            updateState(isLoading = false, value = this)
        }
    }

    // 사용하지 않는 영역 크기.
    fun unusedAreaSize(rotation: Int, width: Int, height: Int): Pair<Int, Int> {
        return if (item.value.cropPercent.size == 2) {
            when (rotation) {
                0, 2 -> {
                    Pair(
                        (width * (1.0f - item.value.cropPercent[0]) * 0.5f).toInt(),
                        (height * (1.0f - item.value.cropPercent[1]) * 0.5f).toInt()
                    )
                }
                else -> {
                    Pair(
                        (width * (1.0f - item.value.cropPercent[1]) * 0.5f).toInt(),
                        (height * (1.0f - item.value.cropPercent[0]) * 0.5f).toInt()
                    )
                }
            }
        } else {
            Pair(0, 0)
        }
    }

    // 이미지 가져오기 가능한.
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
        event(Event.TakeMultiplePictures(item.value.uris, item.value.sizes))
    }

    // 메타를 포함한 출력 옵션 반환.
    fun outputFileOptions(
        lensFacing: Int,
        isPublicDirectory: Boolean = true
    ): ImageCapture.OutputFileOptions {
        val context = getApplication<Application>().applicationContext
        val metadata = ImageCapture.Metadata().apply {
            // 전면 카메라에 대한 반전 설정.
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        // 메타를 포함한 출력 옵션 반환.
        return context.outputFileOptionsBuilder(isPublicDirectory).apply {
            setMetadata(metadata)
        }.build()
    }

    fun saveImage(byteBuffer: ByteBuffer) {
        // 셔터음 이벤트.
        event(Event.PlayShutterSound(item.value.canMute))

        // 이미지 저장.
        val context = getApplication<Application>().applicationContext
        val byteArray = ByteArray(byteBuffer.capacity()).also {
            byteBuffer.get(it)
        }
        val uri = byteArray.save(context, true)?.toUri() ?: Uri.EMPTY

        // Exif: Size.
        val exif = uri.exif(context)
        val size = exif?.let {
            Size(it.width, it.height)
        } ?: Size(0, 0)

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
            value?.copy(
                uris = uris,
                sizes = sizes
            )
        }

        // 촬영 완료.
        if (!item.value.isMultiplePicture) {
            // Thumbnail.
            val thumbnail = uri?.toThumbnail(context, exif)
            // 촬영완료 이벤트.
            event(Event.TakePicture(uri, size, thumbnail))
        }
    }
}