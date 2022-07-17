package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction
import kr.co.kadb.cameralibrary.presentation.widget.extension.mediaScanning
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.toJsonPretty
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

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
//
//    val isEmpty: StateFlow<Boolean> = state.filter { !it.isLoading }
//        .map {
//            it.value?.isMultiplePicture?.isNullOrEmpty()
//        }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
        action?.let {
            // Debug.
            Timber.i(">>>>> ShootSharedViewModel updateState[1] : %s", state.value.value.toJsonPretty())
            val shootUiState = if (state.value.value == null) {
                ShootUiState(isMultiplePicture = it == IntentAction.ACTION_TAKE_MULTIPLE_PICTURE)
            } else {
                state.value.value?.copy(isMultiplePicture = it == IntentAction.ACTION_TAKE_MULTIPLE_PICTURE)
            }
            // Debug.
            Timber.i(">>>>> ShootSharedViewModel updateState[2] : %s", state.value.value.toJsonPretty())
            updateState(value = shootUiState)
            // Debug.
            Timber.i(">>>>> ShootSharedViewModel updateState[3] : %s", state.value.value.toJsonPretty())
        }
    }

    var count = 0

    init {
        Timber.i(">>>>> ShootSharedViewModel init")
    }

    fun genCount() {
        Timber.i(">>>>> ShootSharedViewModel count : %s", count++)
    }


    val paths = mutableListOf<String>()

    // jpeg ByteArray를 파일로 저장.
    fun saveWithByte(bytes: ByteArray, action: (() -> Unit)? = null) {
//		appExecutors.diskIO().execute {
//			bytes.save(getApplication(), true, format = Bitmap.CompressFormat.PNG)?.let { path ->
//				// add items.
//				paths.add(path)
//			}
//			appExecutors.mainThread().execute {
//				action?.invoke()
//			}
//		}

        // 개인정보동의 이미지 처리.
        viewModelScope.launch(Dispatchers.IO) {
            Timber.i(">>>>> Dispatchers.Default : [1]")
            //withContext(Dispatchers.Default) {
            Timber.i(">>>>> Dispatchers.Default : [2]")
            bytes.save(getApplication(), true, format = Bitmap.CompressFormat.PNG)?.let { path ->
                // add items.
                paths.add(path)
            }
            Timber.i(">>>>> Dispatchers.Default : [3]")
            //}
            Timber.i(">>>>> Dispatchers.Default : [4]")

            withContext(Dispatchers.Main) {
                Timber.i(">>>>> Dispatchers.Default : [5]")
                action?.invoke()
            }
            Timber.i(">>>>> Dispatchers.Default : [6]")
        }
    }

    // Bitmap을 파일로 저장.
    fun saveWithBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            // Debug.
            Timber.i(">>>>> Save Bitmap")

            var filepath = ""
            var fileOutputStream: FileOutputStream? = null
            try {
                val directory = File(
                    getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    ""
                )
                if (!directory.mkdirs()) {
                    Timber.e(">>>>> Directory not created : %s", directory)
                }
                filepath = "${directory.absolutePath}/${System.currentTimeMillis()}.jpg"
                fileOutputStream = FileOutputStream(filepath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            } catch (ex: Exception) {
                // Debug.
                Timber.e(ex)
            } finally {
                fileOutputStream?.close()
            }
            bitmap.recycle()

            // Media Scanning.
            getApplication<Application>().mediaScanning(filepath)

            // Debug.
            Timber.i(">>>>> Save Bitmap Finish")
        }
    }
}