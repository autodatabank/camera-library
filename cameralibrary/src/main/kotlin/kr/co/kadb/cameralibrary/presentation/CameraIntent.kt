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
public class CameraIntent {
    // 한 장 촬영.
    @Suppress("DEPRECATION")
    @Deprecated(
        message = "agrs의 직관성을 위하여 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    public fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
        horizonColor: Int? = null,
        unusedAreaBorderColor: Int? = null,
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
    @Suppress("DEPRECATION")
    @Deprecated(
        message = "agrs의 직관성을 위하여 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    public fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        cropPercent: Array<Float>? = null,
        horizonColor: Int? = null,
        unusedAreaBorderColor: Int? = null,
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
    @Suppress("DEPRECATION")
    @Deprecated(
        message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    public fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        isSaveCroppedImage: Boolean? = null,
        cropSize: CropSize? = null,
        horizonColor: Int? = null,
        unusedAreaBorderColor: Int? = null,
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
    @Suppress("DEPRECATION")
    @Deprecated(
        message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
        level = DeprecationLevel.WARNING
    )
    public fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        isSaveCroppedImage: Boolean? = null,
        cropSize: CropSize? = null,
        horizonColor: Int? = null,
        unusedAreaBorderColor: Int? = null,
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

    // 한 장 촬영.
    public fun navigateToTakePicture(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        horizonColor: Int? = null
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
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
        }
    }

    // 여러 장 촬영.
    public fun navigateToTakeMultiplePictures(
        activityContext: Context,
        canMute: Boolean? = null,
        hasHorizon: Boolean? = null,
        canUiRotation: Boolean? = null,
        horizonColor: Int? = null
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
            horizonColor?.let {
                intent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, it)
            }
        }
    }

    // Builder.
    public class Build(private val activityContext: Context) {
        private var action: String? = null
        private var canMute: Boolean? = null
        private var hasHorizon: Boolean? = null
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        private var isSaveCroppedImage: Boolean? = null
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        private var cropPercent: Array<Float>? = null
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        private var cropSize: CropSize? = null
        private var canUiRotation: Boolean? = null
        private var horizonColor: Int? = null
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        private var unusedAreaBorderColor: Int? = null

        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        @IntRange(from = 1, to = 100)
        private var croppedJpegQuality: Int = 95

        public fun setAction(action: String?): Build {
            this.action = action
            return this
        }

        public fun setCanMute(canMute: Boolean?): Build {
            this.canMute = canMute ?: false
            return this
        }

        public fun setHasHorizon(hasHorizon: Boolean?): Build {
            this.hasHorizon = hasHorizon ?: false
            return this
        }

        @Suppress("DEPRECATION")
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        public fun setSaveCropedImage(isSaveCroppedImage: Boolean?): Build {
            this.isSaveCroppedImage = isSaveCroppedImage ?: false
            return this
        }

        @Suppress("DEPRECATION")
        @Deprecated(
            message = "agrs의 직관성을 위하여 Deprecated." +
                    "따라서 setCropPercent(CropSize) 사용하세요.",
            level = DeprecationLevel.WARNING
        )
        public fun setCropPercent(cropPercent: Array<Float>?): Build {
            this.cropPercent = cropPercent
            return this
        }

        @Suppress("DEPRECATION")
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        public fun setCropSize(cropSize: CropSize): Build {
            this.cropSize = cropSize
            return this
        }

        @Suppress("DEPRECATION")
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        public fun setCropSize(
            @FloatRange(from = 0.0, to = 1.0) width: Float,
            @FloatRange(from = 0.0, to = 1.0) height: Float
        ): Build {
            this.cropSize = CropSize(width, height)
            return this
        }

        public fun setCanUiRotation(canUiRotation: Boolean?): Build {
            this.canUiRotation = canUiRotation
            return this
        }

        public fun setHorizonColor(horizonColor: Int?): Build {
            this.horizonColor = horizonColor
            return this
        }

        @Suppress("DEPRECATION")
        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        public fun setUnusedAreaBorderColor(unusedAreaBorderColor: Int?): Build {
            this.unusedAreaBorderColor = unusedAreaBorderColor
            return this
        }

        @Deprecated(
            message = "다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.",
            level = DeprecationLevel.WARNING
        )
        public fun setCroppedJpegQuality(
            @IntRange(
                from = 1,
                to = 100
            ) croppedJpegQuality: Int = 95
        ): Build {
            @Suppress("DEPRECATION")
            this.croppedJpegQuality = croppedJpegQuality
            return this
        }

        /*
         * 다양한 단말에서의 테스트 필요 이슈로 Crop 기능 Deprecated.
         */
        public fun build(): Intent {
            return Intent(activityContext, ShootActivity::class.java).also { cameraIntent ->
                cameraIntent.action = action
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, canMute)
                cameraIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, hasHorizon)
                /*if (cropPercent == null || (cropPercent?.size ?: 0) < 2) {
                    cameraIntent.putExtra(IntentKey.EXTRA_CROP_SIZE, cropSize)
                } else {
                    cameraIntent.putExtra(
                        IntentKey.EXTRA_CROP_SIZE,
                        CropSize(
                            cropPercent?.get(0) ?: 0.0f,
                            cropPercent?.get(1) ?: 0.0f
                        )
                    )
                }*/
                cameraIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, canUiRotation)
                /*cameraIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, isSaveCroppedImage)*/
                cameraIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, horizonColor)
                /*cameraIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, unusedAreaBorderColor)
                cameraIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, croppedJpegQuality)*/
            }
        }
    }
}