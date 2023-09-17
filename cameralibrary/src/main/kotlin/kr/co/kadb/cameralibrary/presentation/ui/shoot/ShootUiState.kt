package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.net.Uri
import android.util.Size
import androidx.annotation.IntRange
import kr.co.kadb.cameralibrary.presentation.model.ShootItem
import kr.co.kadb.cameralibrary.presentation.model.CropSize

/**
 * Created by oooobang on 2022. 7. 17..
 * UI State.
 */
internal data class ShootUiState(
    val action: String?,
    val isDebug: Boolean = false,
    val isShooted: Boolean = false,
    val isMultiplePicture: Boolean = false,
    val isUsingMLKit: Boolean = false,
    val isVehicleNumberPicture: Boolean = false,
    val isMileagePicture: Boolean = false,
    val isVinNumberPicture: Boolean = false,
    val isMaintenanceStatementPicture: Boolean = false,
    val canMute: Boolean = false,
    val hasHorizon: Boolean = false,
    val canUiRotation: Boolean = false,
    val isSaveCroppedImage: Boolean = false,
    val cropSize: CropSize = CropSize.Uninitialized,
    val uris: ArrayList<Uri> = arrayListOf(),
    val sizes: ArrayList<Size> = arrayListOf(),
    val rotations: ArrayList<Int> = arrayListOf(),
    val horizonColor: Int = -1,
    val unusedAreaBorderColor: Int = -1,
    @IntRange(from = 1, to = 100)
    val croppedJpegQuality: Int = 95
) {
    /*class DiffCallback : DiffUtil.ItemCallback<NewsItemUiState>() {
        override fun areItemsTheSame(
            oldItem: ShootUiState,
            newItem: ShootUiState
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ShootUiState,
            newItem: ShootUiState
        ): Boolean = oldItem == newItem
    }*/
    companion object {
        val Uninitialized = ShootUiState(
            action = null,
            isDebug = false,
            isShooted = false,
            isMultiplePicture = false,
            isUsingMLKit = false,
            isVehicleNumberPicture = false,
            isMileagePicture = false,
            isVinNumberPicture = false,
            isMaintenanceStatementPicture = false,
            canMute = false,
            hasHorizon = false,
            canUiRotation = false,
            isSaveCroppedImage = false,
            cropSize = CropSize.Uninitialized,
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = -1,
            unusedAreaBorderColor = -1,
            croppedJpegQuality = 95
        )
    }
}

internal fun List<ShootItem>.toUiState(): List<ShootUiState> = map { it.toUiState() }

internal fun ShootItem.toUiState(): ShootUiState = ShootUiState(
    action = action,
    isDebug = isDebug,
    isShooted = isShooted,
    isMultiplePicture = isMultiplePicture,
    isUsingMLKit = isUsingMLKit,
    isVehicleNumberPicture = isVehicleNumberPicture,
    isMileagePicture = isMileagePicture,
    isVinNumberPicture = isVinNumberPicture,
    isMaintenanceStatementPicture = isMaintenanceStatementPicture,
    canMute = canMute,
    hasHorizon = hasHorizon,
    canUiRotation = canUiRotation,
    isSaveCroppedImage = isSaveCroppedImage,
    cropSize = cropSize,
    uris = uris,
    sizes = sizes,
    rotations = rotations,
    horizonColor = horizonColor,
    unusedAreaBorderColor = unusedAreaBorderColor,
    croppedJpegQuality = croppedJpegQuality
)
