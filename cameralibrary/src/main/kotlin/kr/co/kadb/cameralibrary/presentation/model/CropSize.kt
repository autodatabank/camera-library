package kr.co.kadb.cameralibrary.presentation.model

import androidx.annotation.FloatRange
import java.io.Serializable

data class CropSize constructor(
    @FloatRange(from = 0.0, to = 1.0)
    val width: Float = 0.0f,
    @FloatRange(from = 0.0, to = 1.0)
    val height: Float = 0.0f
) : Serializable {
    // 빈 값 비교.
    val isEmpty = width == 0.0f || height == 0.0f

    // 빈 값 비교.
    val isNotEmpty = width > 0.0f && height > 0.0f

    // 비어 있는 객체.
    companion object {
        val Uninitialized = CropSize(
            width = 0.0f,
            height = 0.0f
        )
    }
}