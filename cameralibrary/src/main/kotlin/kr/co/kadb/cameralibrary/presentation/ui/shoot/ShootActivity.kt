package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.os.Bundle
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseActivity
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.EXTRA_TO

/**
 * Created by oooobang on 2020. 1. 3..
 * 촬영.
 */
internal class ShootActivity : BaseActivity() {
	private val viewController: ShootController by lazy {
		ShootController(this)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.adb_cameralibrary_activity_shoot)

		if (savedInstanceState == null) {
			viewController.navigateToShooting(intent.getStringExtra(EXTRA_TO))
		}
	}
}