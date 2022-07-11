package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.presentation.model.ShootUiState
import kr.co.kadb.cameralibrary.presentation.model.UiState
import kr.co.kadb.cameralibrary.presentation.viewmodel.BaseAndroidViewModel
import kr.co.kadb.cameralibrary.presentation.widget.extension.mediaScanning
import kr.co.kadb.cameralibrary.presentation.widget.extension.save
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Created by oooobang on 2022. 7. 11..
 * ViewModel.
 */
internal class ShootViewModel
constructor(
    application: Application,
    @Suppress("UNUSED_PARAMETER") preferences: PreferenceManager
) : BaseAndroidViewModel<ShootUiState>(application, UiState.loading()) {
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
                val directory = File(getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "")
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