package kr.co.kadb.cameralibrary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kr.co.kadb.cameralibrary.presentation.base.UiState
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog

internal abstract class BaseViewModel<T>(
    initialState: UiState<T>
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(initialState)
    val uiState = _uiStateFlow.asStateFlow()

    protected fun updateState(transform: (T) -> T) {
        DebugLog.i { "ViewModel updateState" }
        val state = uiState.value
        val value = state.value ?: return
        DebugLog.i { ">>>>> ViewModel updateState : $value" }
        _uiStateFlow.update {
            UiState.success(transform(value))
        }
    }

    protected fun updateState(
        isLoading: Boolean = uiState.value.isLoading,
        cause: Throwable? = uiState.value.cause,
        value: T? = uiState.value.value
    ) {
        _uiStateFlow.value = UiState(isLoading = isLoading, cause = cause, value = value)
    }
}
