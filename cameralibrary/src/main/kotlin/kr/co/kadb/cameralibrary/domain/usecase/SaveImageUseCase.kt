package kr.co.kadb.cameralibrary.domain.usecase

import kotlinx.coroutines.flow.*
import kr.co.kadb.cameralibrary.domain.repository.ImageRepository
import kr.co.kadb.cameralibrary.presentation.model.Image
import kr.co.kadb.cameralibrary.presentation.widget.extension.resultOf
import kr.co.kadb.cameralibrary.presentation.widget.extension.toFailedThrowable

// 이미지 저장.
internal class SaveImageUseCase(private val repository: ImageRepository) {

    fun execute(byteArray: ByteArray): Flow<Result<Image>> = repository
        .save(byteArray)
        .map {
            resultOf { it }
        }
        .catch {
            emit(Result.failure(it.toFailedThrowable()))
        }
}