package kr.co.kadb.cameralibrary.presentation.ui

import android.app.Application
import kr.co.kadb.cameralibrary.data.repository.ImageRepositoryImpl

internal class CameraServiceLocator private constructor(application: Application) : ServiceLocator {

    companion object {

        private var instance: CameraServiceLocator? = null

        fun getInstance(application: Application) = instance ?: CameraServiceLocator(
            application
        ).also {
            instance = it
        }
    }

    override val imageRepository: ImageRepositoryImpl by lazy {
        ImageRepositoryImpl(application)
    }
}