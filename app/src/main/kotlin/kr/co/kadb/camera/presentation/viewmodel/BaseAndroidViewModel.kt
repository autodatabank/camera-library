package kr.co.kadb.camera.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kr.co.kadb.camera.presentation.model.UiState
import timber.log.Timber

abstract class BaseAndroidViewModel<T>(
    application: Application,
    initialState: UiState<T>
) : AndroidViewModel(application) {
    private val disposable = CompositeDisposable()

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected fun updateState(transform: (T?) -> T?) {
        //val state = state.value
        // Debug.
        Timber.i(">>>>> BaseAndroidViewModel[1] : %s", state.value)
        Timber.i(">>>>> BaseAndroidViewModel[2] : %s", _state.value)
        //val value = state.value ?: return
        _state.update {
            Timber.i(">>>>> BaseAndroidViewModel[3] : %s", it)
            Timber.i(">>>>> BaseAndroidViewModel[4] : %s", state.value)
            Timber.i(">>>>> BaseAndroidViewModel[5] : %s", _state.value)
            Timber.i(">>>>> BaseAndroidViewModel[6] : %s", transform(state.value.value))
            //Timber.i(">>>>> BaseAndroidViewModel[7] : %s", UiState.success(transform(state.value.value)))
            UiState.success(transform(state.value.value))
        }
    }

    protected fun updateState(
        isLoading: Boolean = state.value.isLoading,
        cause: Throwable? = state.value.cause,
        value: T? = state.value.value
    ) {
        Timber.i(">>>>> BaseAndroidViewModel updateState[1] : %s", value)
        _state.value = UiState(isLoading = isLoading, cause = cause, value = value)
        Timber.i(">>>>> BaseAndroidViewModel updateState[2] : %s", _state.value)
    }

    protected fun addDisposable(disposable: Disposable) {
        this.disposable.add(disposable)
    }

    override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
        super.onCleared()
    }
}