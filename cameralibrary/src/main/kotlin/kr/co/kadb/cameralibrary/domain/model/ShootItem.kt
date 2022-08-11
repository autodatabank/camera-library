package kr.co.kadb.cameralibrary.domain.model

import android.net.Uri
import android.util.Size

internal data class ShootItem(
    val action: String?,
    val isDebug: Boolean,
    val isShooted: Boolean,
    val isMultiplePicture: Boolean,
    val canMute: Boolean,
    val hasHorizon: Boolean,
    val canUiRotation: Boolean,
    val cropPercent: List<Float>,
    val uris: ArrayList<Uri>,
    val sizes: ArrayList<Size>,
    val rotations: ArrayList<Int>,
    val horizonColor: Int,
    val unusedAreaBorderColor: Int
)
