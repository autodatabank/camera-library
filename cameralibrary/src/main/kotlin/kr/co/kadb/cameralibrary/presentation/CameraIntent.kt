package kr.co.kadb.cameralibrary.presentation

import android.content.Context
import android.content.Intent
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootActivity
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey

/**
 * Created by oooobang on 2022. 7. 27..
 * 카메라용 Intent Builder.
 */
class CameraIntent {
    // 한 장 촬영.
    fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
    ): Intent {
        return Intent(activityContext, ShootActivity::class.java).also { intent ->
            intent.action = IntentKey.ACTION_TAKE_PICTURE
            canMute?.let {
                intent.putExtra(IntentKey.EXTRA_CAN_MUTE, it)
            }
            hasHorizon?.let {
                intent.putExtra(IntentKey.EXTRA_HAS_HORIZON, hasHorizon)
            }
            canUiRotation?.let {
                intent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, it)
            }
            cropPercent?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_PERCENT, it)
            }
        }
    }

    // 여러 장 촬영.
    fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
    ): Intent {
        return Intent(activityContext, ShootActivity::class.java).also { intent ->
            intent.action = IntentKey.ACTION_TAKE_MULTIPLE_PICTURES
            canMute?.let {
                intent.putExtra(IntentKey.EXTRA_CAN_MUTE, it)
            }
            hasHorizon?.let {
                intent.putExtra(IntentKey.EXTRA_HAS_HORIZON, hasHorizon)
            }
            canUiRotation?.let {
                intent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, it)
            }
            cropPercent?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_PERCENT, it)
            }
        }
    }

    // Builder.
    class Build(private val activityContext: Context) {
        private var action: String? = null
        private var canMute: Boolean? = null
        private var hasHorizon: Boolean? = null
        private var cropPercent: Array<Float>? = null
        private var canUiRotation: Boolean? = null

        fun setAction(action: String?): Build {
            action?.let {
                this.action = it
            }
            return this
        }

        fun setCanMute(canMute: Boolean?): Build {
            canMute?.let {
                this.canMute = it
            }
            return this
        }

        fun setHasHorizon(hasHorizon: Boolean?): Build {
            hasHorizon?.let {
                this.hasHorizon = it
            }
            return this
        }

        fun setCropPercent(cropPercent: Array<Float>?): Build {
            cropPercent?.let {
                this.cropPercent = it
            }
            return this
        }

        fun setCanUiRotation(canUiRotation: Boolean?): Build {
            canUiRotation?.let {
                this.canUiRotation = it
            }
            return this
        }

        fun build(): Intent {
            return Intent(activityContext, ShootActivity::class.java).also { cameraIntent ->
                cameraIntent.action = action
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, canMute)
                cameraIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, hasHorizon)
                cameraIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, cropPercent)
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, canUiRotation)
            }
        }
    }
}