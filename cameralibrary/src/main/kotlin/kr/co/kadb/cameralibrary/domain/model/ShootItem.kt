package kr.co.kadb.cameralibrary.domain.model

import android.net.Uri
import android.util.Size
import androidx.annotation.IntRange
import kr.co.kadb.cameralibrary.presentation.model.CropSize

internal data class ShootItem(
    val action: String?,
    val isDebug: Boolean,
    val isShooted: Boolean,
    val isMultiplePicture: Boolean,
    val canMute: Boolean,
    val hasHorizon: Boolean,
    val canUiRotation: Boolean,
    val isSaveCroppedImage: Boolean,
    val cropSize: CropSize,
    val uris: ArrayList<Uri>,
    val sizes: ArrayList<Size>,
    val rotations: ArrayList<Int>,
    val horizonColor: Int,
    val unusedAreaBorderColor: Int,
    @IntRange(from = 1, to = 100)
    val croppedJpegQuality: Int
)
