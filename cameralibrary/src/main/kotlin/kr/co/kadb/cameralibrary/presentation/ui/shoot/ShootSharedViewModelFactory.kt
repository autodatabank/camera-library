package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.co.kadb.cameralibrary.PreferenceManager

internal class ShootSharedViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ShootSharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ShootSharedViewModel(
                context.applicationContext as Application,
                PreferenceManager.getInstance(context)
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}

/*internal class ShootSharedViewModelFactory(
    //private val context: Context
    private val serviceLocator: ServiceLocator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ShootSharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ShootSharedViewModel(
                //application = context.applicationContext as Application,
                saveImageUseCase = SaveImageUseCase(
                    repository = serviceLocator.imageRepository
                )*//*,
                preferences = PreferenceManager.getInstance(context)*//*
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}*/
