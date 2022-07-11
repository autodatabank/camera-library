package kr.co.kadb.camera.presentation.ui.shoot

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.presentation.base.BaseActivity
import kr.co.kadb.camera.presentation.widget.util.IntentKey.EXTRA_TO
import javax.inject.Inject

/**
 * Created by oooobang on 2020. 1. 3..
 * 촬영.
 */
//@AndroidEntryPoint
internal class ShootActivity : BaseActivity() {

	@Inject
	lateinit var viewController: ShootController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_shoot)

		if (savedInstanceState == null) {
			viewController.navigateToShooting(intent.getStringExtra(EXTRA_TO))
		}
	}
}