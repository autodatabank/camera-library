package kr.co.kadb.cameralibrary.presentation.model

import androidx.annotation.FloatRange
import java.io.Serializable

public data class CropSize(
    @FloatRange(from = 0.0, to = 1.0)
    val width: Float = 0.0f,
    @FloatRange(from = 0.0, to = 1.0)
    val height: Float = 0.0f
) : Serializable {
    // 빈 값 비교.
    public val isEmpty: Boolean = width == 0.0f || height == 0.0f

    // 빈 값 비교.
    public val isNotEmpty: Boolean = width > 0.0f && height > 0.0f

    // 비어 있는 객체.
    public companion object {
        public val Uninitialized: CropSize = CropSize(
            width = 0.0f,
            height = 0.0f
        )
    }
}