package kr.co.kadb.cameralibrary.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.kadb.cameralibrary.presentation.model.Image

internal interface ImageRepository {

    // 이미지 저장.
    fun save(byteArray: ByteArray): Flow<Image>
}
