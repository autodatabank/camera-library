package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.content.Context
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseController

/**
 * Created by oooobang on 2020. 2. 3..
 * Controller.
 */
internal class ShootController(activityContext: Context) : BaseController(activityContext) {
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