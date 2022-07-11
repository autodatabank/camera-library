package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.co.kadb.cameralibrary.data.local.PreferenceManager

internal class ShootViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ShootViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ShootViewModel(
                context.applicationContext as Application,
                PreferenceManager.getInstance(context)
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}
//
//class ShootViewModelFactory : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T = ShootViewModel(
//
//    ) as T
//}
