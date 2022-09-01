package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseActivity
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey

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

        val defaultHhorizonColor = ContextCompat.getColor(
            applicationContext, R.color.adb_cameralibrary_bg_camera_horizon
        )
        val defaultUnusedAreaBorderColor = ContextCompat.getColor(
            applicationContext, R.color.adb_cameralibrary_bg_camera_unused_area_border
        )

        val action = intent.action
        val isDebug = intent.getBooleanExtra(IntentKey.EXTRA_IS_DEBUG_MODE, false)
        val canMute = intent.getBooleanExtra(IntentKey.EXTRA_CAN_MUTE, false)
        val hasHorizon = intent.getBooleanExtra(IntentKey.EXTRA_HAS_HORIZON, false)
        val canUiRotation = intent.getBooleanExtra(IntentKey.EXTRA_CAN_UI_ROTATION, false)
        val isSaveCroppedImage = intent.getBooleanExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, false)
        val horizonColor = intent.getIntExtra(IntentKey.EXTRA_HORIZON_COLOR, defaultHhorizonColor)
        val unusedAreaBorderColor =
            intent.getIntExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, defaultUnusedAreaBorderColor)

        @Suppress("UNCHECKED_CAST")
        val cropPercent = intent.getSerializableExtra(IntentKey.EXTRA_CROP_PERCENT) as? Array<Float>
        val croppedJpegQuality = intent.getIntExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, 95)
        viewModel.initUiState(
            action,
            isDebug,
            canMute,
            hasHorizon,
            canUiRotation,
            isSaveCroppedImage,
            cropPercent,
            horizonColor,
            unusedAreaBorderColor,
            croppedJpegQuality
        )
        if (savedInstanceState == null) {
            viewController.navigateToShooting()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars())// or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // All below using to hide navigation bar
            val currentApiVersion = Build.VERSION.SDK_INT
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    //or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            // This work only for android 4.4+
            if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
                window.decorView.systemUiVisibility = flags
                val decorView = window.decorView
                decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
            }
        }
    }
}