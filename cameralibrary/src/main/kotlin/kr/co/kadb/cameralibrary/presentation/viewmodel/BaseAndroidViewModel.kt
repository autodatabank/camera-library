package kr.co.kadb.cameralibrary.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kr.co.kadb.cameralibrary.presentation.base.UiState
import timber.log.Timber

internal abstract class BaseAndroidViewModel<T>(
    application: Application,
    initialState: UiState<T>
) : AndroidViewModel(application) {

    /*private val disposable = CompositeDisposable()*/

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected fun updateState(transform: (T) -> T) {
        Timber.i(">>>>> AndroidViewModel updateState")
        val state = state.value
        val value = state.value ?: return
        Timber.i(">>>>> AndroidViewModel updateState : %s", value)
        _state.update {
            UiState.success(transform(value))
        }
    }

    protected fun updateState(
        isLoading: Boolean = state.value.isLoading,
        cause: Throwable? = state.value.cause,
        value: T? = state.value.value
    ) {
        _state.value = UiState(isLoading = isLoading, cause = cause, value = value)
    }

    /*protected fun addDisposable(disposable: Disposable) {
        this.disposable.add(disposable)
    }*/

    /*override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
        super.onCleared()
    }*/
}