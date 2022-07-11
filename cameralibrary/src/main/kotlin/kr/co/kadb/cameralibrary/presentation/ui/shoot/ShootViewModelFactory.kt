package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.co.kadb.cameralibrary.data.local.PreferenceManager

class ShootViewModelFactory(private val application: Application, preferences: PreferenceManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ShootViewModel::class.java)) {
            ShootViewModel(param) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}

class ShootViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ShootViewModel(

    ) as T
}
