package kr.co.kadb.cameralibrary.data.repository

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kr.co.kadb.cameralibrary.CameraLibraryInitProvider
import kr.co.kadb.cameralibrary.domain.repository.ImageRepository
import kr.co.kadb.cameralibrary.presentation.model.Image
import kr.co.kadb.cameralibrary.presentation.widget.extension.saveImage

internal class ImageRepositoryImpl(
    application: Application
) : ImageRepository {

    // 이미지 저장.
    override fun save(byteArray: ByteArray): Flow<Image> = callbackFlow {

        // Save Bitmap.
        byteArray.saveImage(
            CameraLibraryInitProvider().requireContext, true
        ) { savedPath ->
            trySend(
                Image(
                    path = savedPath ?: "",
                    uri = savedPath?.toUri() ?: Uri.EMPTY
                )
            )
        }
        awaitClose()
    }
}