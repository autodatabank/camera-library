package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
import kr.co.kadb.cameralibrary.presentation.widget.extension.pxToDp
import kr.co.kadb.cameralibrary.presentation.widget.extension.toJsonPretty
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.ACTION_TAKE_MULTIPLE_PICTURE
import timber.log.Timber

/**
 * Created by oooobang on 2022. 7. 11..
 * ViewModel.
 */
internal class ShootSharedViewModel
constructor(
    application: Application,
    @Suppress("UNUSED_PARAMETER") preferences: PreferenceManager
) : BaseAndroidViewModel<ShootUiState>(application, UiState.loading()) {

    companion object {
        const val DESIRED_WIDTH_CROP_PERCENT = 10
        const val DESIRED_HEIGHT_CROP_PERCENT = 10
    }

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

    val imageCropPercentages = MutableLiveData<Pair<Int, Int>>().apply {
        value = Pair(DESIRED_WIDTH_CROP_PERCENT, DESIRED_HEIGHT_CROP_PERCENT)
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
        hasMute: Boolean = false,
        cropPercent: Array<Float>?
    ) {
        // Debug.
        Timber.i(">>>>> initUiState action : $action")
        Timber.i(">>>>> initUiState hasMute : $hasMute")
        Timber.i(">>>>> initUiState cropPercent : ${cropPercent.toJsonPretty()}")
//
//        val (unusedAreaWidth, unusedAreaHeight) = if (!cropPercent.isNullOrEmpty()) {
//            if (cropPercent.size == 1) {
//                Pair(0, 0)
//            } else {
//
//            }
//            Pair(0, 0)
//        } else {
//            Pair(0, 0)
//        }

        // Update.
        state.value.value?.let {
            state.value.value?.copy(
                action = action,
                isShooted = false,
                isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE,
                hasMute = hasMute,
                cropPercent= cropPercent?.toList() ?: listOf()
            )
        } ?: ShootUiState(
            action = action,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE,
            hasMute = hasMute,
            cropPercent= cropPercent?.toList() ?: listOf(),
//            unusedAreaWidth = unusedAreaWidth,
//            unusedAreaHeight = unusedAreaHeight,
            uris = arrayListOf(),
            sizes = arrayListOf()
        ).run {
            updateState(value = this)
        }
    }

    fun unusedAreaSize(width: Int, height: Int): Pair<Int, Int> {
        val context = getApplication<Application>().applicationContext
        val (unusedAreaWidth, unusedAreaHeight) = if (item.value.cropPercent.size == 2) {
            Pair(width * item.value.cropPercent[0] * 0.5f, height * item.value.cropPercent[1] * 0.5f)
        } else {
            Pair(0.0f, 0.0f)
        }
        // Debug.
        Timber.i(">>>>> unusedAreaSize size : ${item.value.cropPercent}")
        Timber.i(">>>>> unusedAreaSize width : $unusedAreaWidth")
        Timber.i(">>>>> unusedAreaSize width : $unusedAreaHeight")
        return if (unusedAreaWidth > 0.0f && unusedAreaHeight > 0.0f) {
            return Pair(
                context.pxToDp(unusedAreaWidth.toInt()).toInt(),
                context.pxToDp(unusedAreaHeight.toInt()).toInt()
            )
        } else {
            Pair(0, 0)
        }
//        return if (item.value.cropPercent.size == 2) {
//            Pair((width * item.value.cropPercent[0]).toInt(), (height * item.value.cropPercent[1]).toInt())
//        } else {
//            Pair(0, 0)
//        }
    }

    // 촬영 버튼 누름.
    fun pressedShutter(uri: Uri?, width: Int?, height: Int?) {
        updateState { uiState ->
            val sizes = uiState?.sizes?.apply {
                this.add(Size(width ?: 0, height ?: 0))
            } ?: arrayListOf()
            val uris = uiState?.uris?.apply {
                this.add(uri?.toString() ?: "")
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