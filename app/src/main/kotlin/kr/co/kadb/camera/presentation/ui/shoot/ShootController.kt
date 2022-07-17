package kr.co.kadb.camera.presentation.ui.shoot

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import kr.co.kadb.camera.R
import kr.co.kadb.camera.presentation.base.BaseController
import javax.inject.Inject

/**
 * Created by oooobang on 2022. 7. 17..
 * Controller.
 */
internal class ShootController
@Inject
constructor(@ActivityContext activityContext: Context) : BaseController(activityContext) {
    // ContainerId.
    private val containerId: Int = R.id.layout_container

    // 촬영.
    fun navigateToShooting() {
        val fragment = ShootFragment.create()
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commitAllowingStateLoss()
    }
}