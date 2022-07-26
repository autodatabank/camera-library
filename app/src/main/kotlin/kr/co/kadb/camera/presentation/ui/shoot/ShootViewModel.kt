package kr.co.kadb.camera.presentation.ui.shoot

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.presentation.model.ShootUiState
import kr.co.kadb.camera.presentation.model.UiState
import kr.co.kadb.camera.presentation.viewmodel.BaseAndroidViewModel
import javax.inject.Inject

/**
 * Created by oooobang on 2020. 2. 3..
 * ViewModel.
 */
@HiltViewModel
internal class ShootViewModel
@Inject
constructor(
    application: Application,
    @Suppress("UNUSED_PARAMETER") preferences: PreferenceManager
) : BaseAndroidViewModel<ShootUiState>(application, UiState.loading()) {
}