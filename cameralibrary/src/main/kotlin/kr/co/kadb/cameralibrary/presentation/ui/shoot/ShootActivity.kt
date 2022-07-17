package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.os.Bundle
import androidx.activity.viewModels
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseActivity

/**
 * Created by oooobang on 2022. 7. 16..
 * 촬영.
 */
internal class ShootActivity : BaseActivity() {
	// ViewController.
	private val viewController: ShootController by lazy {
		ShootController(this)
	}

	// ViewModel.
	private val viewModel: ShootSharedViewModel by viewModels {
		ShootSharedViewModelFactory(this)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.adb_cameralibrary_activity_shoot)

//		viewModel.extras = intent.action
		viewModel.intentAction(intent.action)
		if (savedInstanceState == null) {
			viewController.navigateToShooting()
		}
	}
}