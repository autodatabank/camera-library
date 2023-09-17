package kr.co.kadb.cameralibrary.presentation

import android.content.Context
import android.content.Intent
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.ui.shoot.ShootActivity
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey

/**
 * Created by oooobang on 2022. 7. 27..
 * 카메라용 Intent Builder.
 */
class CameraIntent {
    // 한 장 촬영.
    @Deprecated(
        message = "agrs의 직관성을 위하여 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
        horizonColor: Array<Float>? = null,
        unusedAreaBorderColor: Array<Float>? = null,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int = 95
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
            if (cropPercent != null && cropPercent.size >= 2) {
                intent.putExtra(IntentKey.EXTRA_CROP_SIZE, CropSize(cropPercent[0], cropPercent[1]))
            }
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
            unusedAreaBorderColor?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, it)
            }
            intent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)
        }
    }

    // 여러 장 촬영.
    @Deprecated(
        message = "agrs의 직관성을 위하여 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
        horizonColor: Array<Float>? = null,
        unusedAreaBorderColor: Array<Float>? = null,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int = 95
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
            if (cropPercent != null && cropPercent.size >= 2) {
                intent.putExtra(IntentKey.EXTRA_CROP_SIZE, CropSize(cropPercent[0], cropPercent[1]))
            }
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
            unusedAreaBorderColor?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, it)
            }
            intent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)
        }
    }

    // 한 장 촬영.
    fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        isSaveCroppedImage: Boolean? = null,
        cropSize: CropSize? = null,
        horizonColor: Array<Float>? = null,
        unusedAreaBorderColor: Array<Float>? = null,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int = 95
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
            isSaveCroppedImage?.let {
                intent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, it)
            }
            cropSize?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_SIZE, it)
            }
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
            unusedAreaBorderColor?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, it)
            }
            intent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)
        }
    }

    // 여러 장 촬영.
    fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        isSaveCroppedImage: Boolean? = null,
        cropSize: CropSize? = null,
        horizonColor: Array<Float>? = null,
        unusedAreaBorderColor: Array<Float>? = null,
        @IntRange(from = 1, to = 100)
        croppedJpegQuality: Int = 95
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
            isSaveCroppedImage?.let {
                intent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, it)
            }
            cropSize?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_SIZE, it)
            }
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
            unusedAreaBorderColor?.let {
                intent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, it)
            }
            intent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)
        }
    }

    // Builder.
    class Build(private val activityContext: Context) {
        private var action: String? = null
        private var canMute: Boolean? = null
        private var hasHorizon: Boolean? = null
        private var isSaveCroppedImage: Boolean? = null
        private var cropPercent: Array<Float>? = null
        private var cropSize: CropSize? = null
        private var canUiRotation: Boolean? = null
        private var horizonColor: Int? = null
        private var unusedAreaBorderColor: Int? = null

        @IntRange(from = 1, to = 100)
        private var croppedJpegQuality: Int = 95

        fun setAction(action: String?): Build {
            this.action = action
            return this
        }

        fun setCanMute(canMute: Boolean?): Build {
            this.canMute = canMute ?: false
            return this
        }

        fun setHasHorizon(hasHorizon: Boolean?): Build {
            this.hasHorizon = hasHorizon ?: false
            return this
        }

        fun setSaveCropedImage(isSaveCroppedImage: Boolean?): Build {
            this.isSaveCroppedImage = isSaveCroppedImage ?: false
            return this
        }

        @Deprecated(
            message = "agrs의 직관성을 위하여 Deprecated." +
                    "따라서 setCropPercent(CropSize) 사용하세요.",
            level = DeprecationLevel.WARNING
        )
        fun setCropPercent(cropPercent: Array<Float>?): Build {
            this.cropPercent = cropPercent
            return this
        }

        fun setCropSize(cropSize: CropSize): Build {
            this.cropSize = cropSize
            return this
        }

        fun setCropSize(
            @FloatRange(from = 0.0, to = 1.0) width: Float,
            @FloatRange(from = 0.0, to = 1.0) height: Float
        ): Build {
            this.cropSize = CropSize(width, height)
            return this
        }

        fun setCanUiRotation(canUiRotation: Boolean?): Build {
            this.canUiRotation = canUiRotation
            return this
        }

        fun setHorizonColor(horizonColor: Int?): Build {
            this.horizonColor = horizonColor
            return this
        }

        fun setUnusedAreaBorderColor(unusedAreaBorderColor: Int?): Build {
            this.unusedAreaBorderColor = unusedAreaBorderColor
            return this
        }

        fun setCroppedJpegQuality(
            @IntRange(
                from = 1,
                to = 100
            ) croppedJpegQuality: Int = 95
        ): Build {
            this.croppedJpegQuality = croppedJpegQuality
            return this
        }

        fun build(): Intent {
            return Intent(activityContext, ShootActivity::class.java).also { cameraIntent ->
                cameraIntent.action = action
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, canMute)
                cameraIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, hasHorizon)
                if (cropPercent == null || (cropPercent?.size ?: 0) < 2) {
                    cameraIntent.putExtra(IntentKey.EXTRA_CROP_SIZE, cropSize)
                } else {
                    cameraIntent.putExtra(
                        IntentKey.EXTRA_CROP_SIZE,
                        CropSize(
                            cropPercent?.get(0) ?: 0.0f,
                            cropPercent?.get(1) ?: 0.0f
                        )
                    )
                }
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, canUiRotation)
                cameraIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, isSaveCroppedImage)
                cameraIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, horizonColor)
                cameraIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, unusedAreaBorderColor)
                cameraIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)
            }
        }
    }
}