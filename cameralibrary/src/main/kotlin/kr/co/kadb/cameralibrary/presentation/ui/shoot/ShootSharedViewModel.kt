package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.impl.utils.Exif
import androidx.core.net.toFile
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction
import kr.co.kadb.cameralibrary.presentation.widget.extension.outputFileOptionsBuilder
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.InputStream

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

    init {
        // UIState.
        viewModelScope.launch {
            item.collect { item ->
                // Debug.
                Timber.i(">>>>> ShootSharedViewModel item : %s", item)
            }
        }
    }

    init {
        Timber.i(">>>>> ShootSharedViewModel init")
    }

    fun intentAction(action: String?) {
        // Debug.
        Timber.i(">>>>> ACTION : %s", action)
        action?.let {
            val shootUiState = if (state.value.value == null) {
                ShootUiState(
                    action = action,
                    isShooted = false,
                    isMultiplePicture = it == IntentAction.ACTION_TAKE_MULTIPLE_PICTURE
                )
            } else {
                state.value.value?.copy(isMultiplePicture = it == IntentAction.ACTION_TAKE_MULTIPLE_PICTURE)
            }
            updateState(value = shootUiState)
        }
    }

    fun pressedShutter() {
        updateState {

        }
    }

    // OutputFileOptions.Builder.
    fun outputFileOptions(lensFacing: Int, isPublicDirectory: Boolean = true): ImageCapture.OutputFileOptions {
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

    fun resize(uri: Uri, resize: Int): Bitmap? {
        val context = getApplication<Application>().applicationContext
        var resizeBitmap: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        try {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

            // 1번
            var width = options.outWidth
            var height = options.outHeight
            var samplesize = 1
            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize) {
                    break
                }
                width /= 2
                height /= 2
                samplesize *= 2
            }
            options.inSampleSize = samplesize;
            val bitmap: Bitmap? = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

            //3번
            resizeBitmap = bitmap
        } catch (ex: FileNotFoundException) {
            ex.printStackTrace()
        }
        return resizeBitmap

    }
}