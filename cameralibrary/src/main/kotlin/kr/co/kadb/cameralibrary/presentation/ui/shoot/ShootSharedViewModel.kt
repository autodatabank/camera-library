package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
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
        state.value.value?.let {
            state.value.value?.copy(
                action = action,
                isShooted = false,
                isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURES,
                canMute = canMute,
                hasHorizon = hasHorizon,
                canUiRotation = canUiRotation,
                cropPercent = cropPercent?.toList() ?: listOf()
            )
        } ?: ShootUiState(
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
            updateState(value = this)
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

    fun toUri(byteBuffer: ByteBuffer) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            ByteArray(byteBuffer.capacity()).also {
                byteBuffer.get(it)
            }.let {
                it.save(context, true)?.toUri()
            }?.run {
                val exif = this.exif(context)
                updateState { uiState ->
                    val sizes = uiState?.sizes?.also {
                        it.add(Size(exif?.width ?: 0, exif?.height ?: 0))
                    } ?: arrayListOf()
                    val uris = uiState?.uris?.also {
                        it.add(this)
                    } ?: arrayListOf()
                    state.value.value?.copy(
                        isShooted = true,
                        uris = uris,
                        sizes = sizes
                    )
                }
            }
        }
    }

    // 촬영 버튼 누름.
    fun pressedShutter() {
        updateState {
            state.value.value?.copy(
                isShooted = true
            )
        }
    }

    // 촬영 버튼 누름.
    fun pressedShutter(uri: Uri?, width: Int?, height: Int?) {
        updateState { uiState ->
            val sizes = uiState?.sizes?.apply {
                this.add(Size(width ?: 0, height ?: 0))
            } ?: arrayListOf()
            val uris = uiState?.uris?.apply {
                this.add(uri ?: Uri.EMPTY)
            } ?: arrayListOf()
            state.value.value?.copy(
                isShooted = true,
                uris = uris,
                sizes = sizes
            )
        }
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
}