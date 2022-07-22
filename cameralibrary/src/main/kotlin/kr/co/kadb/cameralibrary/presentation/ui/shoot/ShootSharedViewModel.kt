package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
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
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction.ACTION_TAKE_MULTIPLE_PICTURE
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
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

    fun intentAction(action: String?) {
        // Debug.
        Timber.i(">>>>> ACTION : %s", action)

        // Update.
        state.value.value?.let {
            state.value.value?.copy(
                action = action,
                isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE
            )
        } ?: ShootUiState(
            action = action,
            isShooted = false,
            isMultiplePicture = action == ACTION_TAKE_MULTIPLE_PICTURE
        ).run {
            updateState(value = this)
        }
    }

    fun pressedShutter() {
        updateState {
            state.value.value?.copy(isShooted = true)
        }
    }

    // OutputFileOptions.
    fun outputFileOptions(
        lensFacing: Int,
        isPublicDirectory: Boolean = true
    ): ImageCapture.OutputFileOptions {
        val context = getApplication<Application>().applicationContext
        // Setup image capture metadata
        val metadata = ImageCapture.Metadata().apply {
            // Mirror image when using the front camera
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        // Create output options object which contains file + metadata
        return context.outputFileOptionsBuilder(isPublicDirectory).apply {
            setMetadata(metadata)
        }.build()
    }
}