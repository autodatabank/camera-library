package kr.co.kadb.cameralibrary.presentation.base

internal data class UiState<T>(
    val isLoading: Boolean = false,
    val cause: Throwable? = null,
    val value: T? = null
) {
    val isError: Boolean = cause != null

    fun update(transform: (value: T) -> T): UiState<T> {
        return if (value != null) {
            success(transform(value))
        } else {
            this
        }
    }

    fun onLoading(block: () -> Unit): UiState<T> = apply {
        if (isLoading) {
            block()
        }
    }

    fun onSuccess(block: (T) -> Unit): UiState<T> = apply {
        if (!isError && !isLoading && value != null) {
            block(value)
        }
    }

    fun onError(block: () -> Unit) = apply {
        if (isError) {
            block()
        }
    }

    fun getOrDefault(defaultValue: T): T {
        return value ?: defaultValue
    }

    fun getOrThrow(): T {
        return requireNotNull(value)
    }

    companion object {
        fun <T> loading() = UiState<T>(isLoading = true)

        fun <T> success(value: T?) = UiState(value = value)

        fun <T> error(cause: Throwable) = UiState<T>(cause = cause)
    }
}