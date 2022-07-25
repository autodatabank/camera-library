package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
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
        cropPercent: List<Float>
    ) {
        // Debug.
        Timber.i(">>>>> ACTION : %s", action)

        val (unusedAreaWidth, unusedAreaHeight) = if (cropPercent.isNotEmpty()) {
            if (cropPercent.size == 1) {
                Pair(0, 0)
            } else {

            }
            Pair(0, 0)
        } else {
            Pair(0, 0)
        }

        // Update.
        state.value.value?.let {
            state.value.value?.copy(
                action = action,
                isShooted = false,
                isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE,
                hasMute = hasMute,
                cropPercent= cropPercent
            )
        } ?: ShootUiState(
            action = action,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE,
            hasMute = hasMute,
            cropPercent= listOf(),
            unusedAreaWidth = unusedAreaWidth,
            unusedAreaHeight = unusedAreaHeight,
            uris = arrayListOf(),
            sizes = arrayListOf()
        ).run {
            updateState(value = this)
        }
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