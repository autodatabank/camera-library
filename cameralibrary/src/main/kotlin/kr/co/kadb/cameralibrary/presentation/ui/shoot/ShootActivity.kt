package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseActivity
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.EXTRA_TO
import javax.inject.Inject

/**
 * Created by oooobang on 2020. 1. 3..
 * 촬영.
 */
@AndroidEntryPoint
internal class ShootActivity : BaseActivity() {

	@Inject
	lateinit var viewController: ShootController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.adb_cameralibrary_activity_shoot)

		if (savedInstanceState == null) {
			viewController.navigateToShooting(intent.getStringExtra(EXTRA_TO))
		}
	}
}