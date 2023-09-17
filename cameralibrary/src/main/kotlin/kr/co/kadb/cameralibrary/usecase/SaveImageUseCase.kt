package kr.co.kadb.cameralibrary.usecase

import androidx.annotation.IntRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.kadb.cameralibrary.presentation.model.Image
import kr.co.kadb.cameralibrary.repository.ImageRepository
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.widget.extension.resultOf
import kr.co.kadb.cameralibrary.presentation.widget.extension.toFailedThrowable

// 이미지 저장.
internal class SaveImageUseCase constructor(private val repository: ImageRepository) {

    fun execute(
        byteArray: ByteArray,
        cropSize: CropSize,
        @IntRange(from = 1, to = 100) croppedJpegQuality: Int
    ): Flow<Result<Image>> = repository
        .save(byteArray, cropSize, croppedJpegQuality)
        .flowOn(Dispatchers.Default)
        .map {
            resultOf { it }
        }
        .catch {
            emit(Result.failure(it.toFailedThrowable()))
        }
}