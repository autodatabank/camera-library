package kr.co.kadb.cameralibrary.presentation.ui

import kr.co.kadb.cameralibrary.data.repository.ImageRepositoryImpl

internal interface ServiceLocator {

    val imageRepository: ImageRepositoryImpl
}